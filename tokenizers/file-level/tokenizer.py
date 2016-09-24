import logging
import multiprocessing as mp
from multiprocessing import Process, Value, Queue
import re
import os
import collections
from lockfile import LockFile
import tarfile
import sys
import hashlib
import datetime as dt

file_count = 0

try:
    from configparser import ConfigParser
except ImportError:
    from ConfigParser import ConfigParser # ver. < 3.0

config_file = sys.argv[1]

# instantiate
config = ConfigParser()

# parse existing file
try:
    config.read(config_file)
except IOError:
    print 'ERROR - Config settings not found. Usage: $python this-script.py config-file.ini'
    sys.exit()

# Get info from config.ini into global variables
N_PROCESSES = config.getint('Main', 'N_PROCESSES')
PROJECTS_BATCH = config.getint('Main', 'PROJECTS_BATCH')
FILE_projects_list = config.get('Main', 'FILE_projects_list')
PATH_stats_file_folder = config.get('Folders/Files', 'PATH_stats_file_folder')
PATH_bookkeeping_proj_folder = config.get('Folders/Files', 'PATH_bookkeeping_proj_folder')
PATH_tokens_file_folder = config.get('Folders/Files', 'PATH_tokens_file_folder')
PATH_logs = config.get('Folders/Files', 'PATH_logs')
# Reading Language settings
separators = config.get('Language', 'separators').strip('"').split(' ')

comment_inline = config.get('Language', 'comment_inline')
comment_open_tag = re.escape(config.get('Language', 'comment_open_tag'))
comment_close_tag = re.escape(config.get('Language', 'comment_close_tag'))
file_extensions = config.get('Language', 'File_extensions').split(' ')

def get_proj_stats_helper(process_num, proj_id, proj_path, file_id_global_var, FILE_tokens_file, FILE_bookkeeping_proj, FILE_stats_file, logging):
    global file_count
    proj_path, proj_url = proj_path

    logging.info('Starting project <'+proj_id+','+proj_path+'> (process '+process_num+')')
    p_start = dt.datetime.now()
    zip_time = file_time = string_time = tokens_time = hash_time = write_time = regex_time = 0

    if not os.path.isdir(proj_path):
        logging.error('Unable to open project <'+proj_id+','+proj_path+'> (process '+process_num+')')
        return

    # Search for all tar files
    tar_files = [os.path.join(proj_path, f) for f in os.listdir(proj_path) if os.path.isfile(os.path.join(proj_path, f))]
    tar_files = [f for f in tar_files if '_code' in f]
    if(len(tar_files) != 1):
        logging.error('Tar not found on <'+proj_id+','+proj_path+'> (process '+process_num+')')
        # Important to have a global loc on this file because it is shared
        return

    tar_file = tar_files[0]

    try:
        with tarfile.open(tar_file,'r') as my_tar_file:
            # Get all members on the tar file
            all_files = []
            for member in my_tar_file.getmembers():
                all_files.append(member.name)

            # Filter them by the correct extension
            aux = []
            for extension in file_extensions:
                aux.extend([x for x in all_files if x.endswith(extension)])
            all_files = aux

            # This is very strange, but I did find some paths with newlines,
            # so I am simply eliminatins them
            all_files = [x for x in all_files if '\n' not in x]

            with file_id_global_var.get_lock():
                all_files = zip(range(file_id_global_var.value, len(all_files)+file_id_global_var.value),all_files)
                file_id_global_var.value = len(all_files)+file_id_global_var.value

            for file_id, file_path in all_files:

                #logging.info('Starting file <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'> (process '+process_num+')')

                file_bytes = 'ERROR'
                file_bytes=str(my_tar_file.getmember(file_path).size)

                z_time = dt.datetime.now();
                try:
                    myfile = my_tar_file.extractfile(file_path)
                except:
                    logging.error('Unable to open file (1) <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'> (process '+process_num+')')
                    break
                zip_time += (dt.datetime.now() - z_time).microseconds

                if myfile is None:
                    logging.error('Unable to open file (2) <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'> (process '+process_num+')')
                    break

                file_count += 1

                f_time = dt.datetime.now()
                file_string = myfile.read()
                file_time += (dt.datetime.now() - f_time).microseconds

                file_hash = 'ERROR'
                lines = 'ERROR'
                LOC = 'ERROR'
                SLOC = 'ERROR'
                file_url = proj_url + '/' + file_path[7:].replace(' ','%20')
                file_path = os.path.join(tar_file,file_path)

                h_time = dt.datetime.now()
                m = hashlib.md5()
                m.update(file_string)
                file_hash = m.hexdigest()
                hash_time += (dt.datetime.now() - h_time).microseconds

                lines = str(file_string.count('\n'))
                file_string = os.linesep.join( [s for s in file_string.splitlines() if s] )
                LOC = str(file_string.count('\n'))

                re_time = dt.datetime.now()
                # Remove tagged comments
                file_string = re.sub(re.escape(comment_open_tag) + '.*?' + re.escape(comment_close_tag), '', file_string, flags=re.DOTALL)
                # Remove enf of line comments
                file_string = re.sub(comment_inline + '.*?\n', '', file_string, flags=re.DOTALL)
                regex_time += (dt.datetime.now() - re_time).microseconds

                SLOC = str(file_string.count('\n'))

                FILE_stats_file.write(','.join([proj_id,str(file_id),file_path,file_url,file_hash,file_bytes,lines,LOC,SLOC])+'\n')

                # Rather a copy of the file string here for tokenization
                file_string_for_tokenization = file_string

                #Transform separators into spaces (remove them)
                s_time = dt.datetime.now()
                for x in separators:
                    file_string_for_tokenization = file_string_for_tokenization.replace(x,' ')
                string_time += (dt.datetime.now() - s_time).microseconds

                ##Create a list of tokens
                file_string_for_tokenization = file_string_for_tokenization.split()
                ## Total number of tokens
                tokens_count_total = str(len(file_string_for_tokenization))
                ##Count occurrences
                file_string_for_tokenization = collections.Counter(file_string_for_tokenization)
                ##Converting Counter to dict because according to StackOverflow is better
                file_string_for_tokenization=dict(file_string_for_tokenization)
                ## Unique number of tokens
                tokens_count_unique = str(len(file_string_for_tokenization))

                t_time = dt.datetime.now()
                #SourcererCC formatting
                tokens = ','.join(['{}@@::@@{}'.format(k, v) for k,v in file_string_for_tokenization.iteritems()])
                tokens_time += (dt.datetime.now() - t_time).microseconds

                # MD5
                h_time = dt.datetime.now()
                m = hashlib.md5()
                m.update(tokens)
                hash_time += (dt.datetime.now() - h_time).microseconds

                entry = ','.join([proj_id,str(file_id),tokens_count_total,tokens_count_unique,m.hexdigest()+'@#@'+tokens])
                w_time = dt.datetime.now()
                FILE_tokens_file.write(entry)
                write_time += (dt.datetime.now() - w_time).microseconds

    except Exception as e:
        logging.error('Unable to open tar on <'+proj_id+','+proj_path+'> (process '+process_num+')')
        logging.error(e)
        return

    FILE_bookkeeping_proj.write(proj_id+','+proj_path+','+proj_url+'\n')

    p_elapsed = (dt.datetime.now() - p_start).microseconds
    logging.info('Project finished <%s,%s> (process %s)', proj_id, proj_path, process_num)
    logging.info(' (%s): Total: %smicros | Zip: %s Read: %s Separators: %smicros Tokens: %smicros Write: %smicros Hash: %s regex: %s', 
                 process_num,  p_elapsed, zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time)

def get_project_stats(process_num, list_projects, file_id_global_var, global_queue):
    # Logging code
    FORMAT = '[%(levelname)s] (%(threadName)s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)
    file_handler = logging.FileHandler(os.path.join(PATH_logs,'LOG-'+process_num+'.log'))
    file_handler.setFormatter(logging.Formatter(FORMAT))
    logging.getLogger().addHandler(file_handler)

    FILE_files_stats_file = os.path.join(PATH_stats_file_folder,'files-stats-'+str(process_num)+'.stats')
    FILE_bookkeeping_proj_name = os.path.join(PATH_bookkeeping_proj_folder,'bookkeeping-proj-'+str(process_num)+'.projs')
    FILE_files_tokens_file = os.path.join(PATH_tokens_file_folder,'files-tokens-'+str(process_num)+'.tokens')

    with open(FILE_files_tokens_file, 'a+') as FILE_tokens_file, \
            open(FILE_bookkeeping_proj_name, 'a+') as FILE_bookkeeping_proj, \
            open(FILE_files_stats_file, 'a+') as FILE_stats_file:
        logging.info("Process %s starting", process_num)
        p_start = dt.datetime.now()
        for proj_id, proj_path in list_projects:
            get_proj_stats_helper(process_num, str(proj_id), proj_path, file_id_global_var, FILE_tokens_file, FILE_bookkeeping_proj, FILE_stats_file, logging)

    p_elapsed = (dt.datetime.now() - p_start).seconds
    logging.info('Process %s finished. %s files in %ss.', 
                 process_num, file_count, p_elapsed)

    # Let parent know, so I can be killed
    global_queue.put((int(process_num), file_count))


def kill_child(processes, pid, n_files_processed):
    global file_count
    file_count += n_files_processed
    if processes[pid] != None:
        processes[pid].terminate()
        processes[pid] = None
        print "Process %s finished, %s files processed (current total: %s)" % (pid, n_files_processed, file_count)


if __name__ == '__main__':

    p_start = dt.datetime.now()

    if os.path.exists(PATH_stats_file_folder) or os.path.exists(PATH_bookkeeping_proj_folder) or os.path.exists(PATH_tokens_file_folder) or os.path.exists(PATH_logs):
        print 'ERROR - Folder ['+PATH_stats_file_folder+'] or ['+PATH_bookkeeping_proj_folder+'] or ['+PATH_tokens_file_folder+'] or ['+PATH_logs+'] already exists!'
        sys.exit()
    else:
        os.makedirs(PATH_stats_file_folder)
        os.makedirs(PATH_bookkeeping_proj_folder)
        os.makedirs(PATH_tokens_file_folder)
        os.makedirs(PATH_logs)

    proj_paths = []
    with open(FILE_projects_list) as f:
        for line in f:
            line_split = line[:-1].split(',') # [:-1] to strip final character which is '\n'
            proj_paths.append((line_split[0],line_split[4]))

    proj_paths = zip(range(1, len(proj_paths)+1),proj_paths)

    #Split list of projects into N_PROCESSES lists
    #proj_paths_list = [ proj_paths[i::N_PROCESSES] for i in xrange(N_PROCESSES) ]

    # Multiprocessing with N_PROCESSES
    processes = [None for i in xrange(N_PROCESSES)]
    # Multiprocessing shared variable instance for recording file_id
    file_id_global_var = Value('i', 1)
    # The queue for processes to communicate back to the parent (this process)
    # Initialize it with N_PROCESSES number of (process_id, n_files_processed)
    global_queue = Queue()
    for i in xrange(N_PROCESSES):
        global_queue.put((i, 0))

    process_num = 0

    while len(proj_paths) > 0:
        # This is a blocking get. If the queue is empty, it waits
        pid, n_files_processed = global_queue.get()
        # OK, one of the processes finished. Let's get its data and kill it
        kill_child(processes, pid, n_files_processed)

        # Get a new batch of project paths ready
        input_process = proj_paths[:PROJECTS_BATCH]
        del proj_paths[:PROJECTS_BATCH]

        print "Starting new process %s" % pid
        p = Process(name='Process '+str(pid), target=get_project_stats, args=(str(pid),input_process, file_id_global_var, global_queue, ))
        processes[pid] = p
        p.start()

    print "No more projects to process. Waiting for children to finish..."
    while processes.count(None) < N_PROCESSES:
        pid, n_files_processed = global_queue.get()
        kill_child(processes, pid, n_files_processed)

    p_elapsed = (dt.datetime.now() - p_start).seconds
    print "All done. %s files in %ssec" % (file_count, p_elapsed)
