import logging
import multiprocessing as mp
import re
import os
import collections
import tarfile
import sys
import hashlib
import datetime as dt
import MySQLdb
import ConfigParser
import zipfile
from db import DB

class Tokenizer(object):

    def __init__(self, proj_paths, DB_user, DB_pass, DB_name, controller_logging, logs_folder, output_folder, N_PROCESSES, PROJECTS_BATCH, PROJECTS_CONFIGURATION):
        self.controller_logging = controller_logging

        self.project_id = 1
        self.proj_paths = []
        self.filecount = 0
        self.logs_folder = ''
        self.output_folder = '' # This output will be the input of CC
        self.PATH_config_file = os.path.join(os.path.dirname(os.path.realpath(__file__)),'config.ini')

        self.N_PROCESSES = PROJECTS_BATCH
        self.PROJECTS_BATCH = PROJECTS_BATCH

        self.DB_user = DB_user
        self.DB_pass = DB_pass
        self.DB_name = DB_name

        if PROJECTS_CONFIGURATION not in ['Leidos','GithubZIP']:
            controller_logging.error('Unknown project configuration format:%s' % (PROJECTS_CONFIGURATION))
            sys.exist(1)
        else:
            self.PROJECTS_CONFIGURATION = PROJECTS_CONFIGURATION

        try:
            db = DB(DB_user,DB_name,DB_pass,logging)
            self.project_id = db.get_max_project_id()
            if self.project_id is None:
                self.project_id = 0
    
            self.project_id += 1

            db.close()

        except Exception as e:
            controller_logging.error('Error on Tokenizer.__init__')
            self.controller_logging.error(e)
            sys.exit(1)

        self.proj_paths = list(proj_paths)

        # Creating folder for the processes logs
        self.logs_folder = logs_folder
        if not os.path.exists( self.logs_folder ):
            controller_logging.error('ERROR - Folder [%s] does not exist!' % self.logs_folder )
            sys.exit(1)

        # Create folder for processes output
        self.output_folder = output_folder
        if not os.path.exists( self.output_folder ):
            controller_logging.error('ERROR - Folder [%s] does not exist!' % self.output_folder )
            sys.exit(1)

        # Reading config file
        config = ConfigParser.ConfigParser()

        try:
            config.read(self.PATH_config_file)
        except Exception as e:
            controller_logging.error('ERROR on Tokenizer.__init__')
            controller_logging.error(e)
            sys.exit(1)

        comment_inline         = re.escape(config.get('Language', 'comment_inline'))
        comment_open_tag       = re.escape(config.get('Language', 'comment_open_tag'))
        comment_close_tag      = re.escape(config.get('Language', 'comment_close_tag'))
        self.separators                 = config.get('Language', 'separators').strip('"').split(' ')
        self.comment_inline_pattern     = comment_inline + '.*?$'
        self.comment_open_close_pattern = comment_open_tag + '.*?' + comment_close_tag
        self.file_extensions            = config.get('Language', 'File_extensions').split(' ')

        controller_logging.info('Tokenizer successfully initialized. Project index starting at %s. Processing %s projects. Looking for file extensions: %s' % (self.project_id, len(self.proj_paths), self.file_extensions) )

    def tokenize(self, file_string, comment_inline_pattern, comment_open_close_pattern, separators):
        final_stats  = 'ERROR'
        final_tokens = 'ERROR'

        file_hash = 'ERROR'
        lines     = 'ERROR'
        LOC       = 'ERROR'
        SLOC      = 'ERROR'

        lines = file_string.count('\n')
        if not file_string.endswith('\n'):
            lines += 1
        file_string = "".join([s for s in file_string.splitlines(True) if s.strip()])

        LOC = file_string.count('\n')
        if not file_string.endswith('\n'):
            LOC += 1

        re_time = dt.datetime.now()
        # Remove tagged comments
        file_string = re.sub(comment_open_close_pattern, '', file_string, flags=re.DOTALL)
        # Remove end of line comments
        file_string = re.sub(comment_inline_pattern, '', file_string, flags=re.MULTILINE)
        re_time = (dt.datetime.now() - re_time).microseconds
    
        file_string = "".join([s for s in file_string.splitlines(True) if s.strip()]).strip()
    
        SLOC = file_string.count('\n')
        if file_string != '' and not file_string.endswith('\n'):
            SLOC += 1

        final_stats = (lines,LOC,SLOC)

        # Rather a copy of the file string here for tokenization
        file_string_for_tokenization = file_string

        #Transform separators into spaces (remove them)
        s_time = dt.datetime.now()
        for x in separators:
            file_string_for_tokenization = file_string_for_tokenization.replace(x,' ')
        s_time = (dt.datetime.now() - s_time).microseconds

        ##Create a list of tokens
        file_string_for_tokenization = file_string_for_tokenization.split()
        ## Total number of tokens
        tokens_count_total = len(file_string_for_tokenization)
        ##Count occurrences
        file_string_for_tokenization = collections.Counter(file_string_for_tokenization)
        ##Converting Counter to dict because according to StackOverflow is better
        file_string_for_tokenization=dict(file_string_for_tokenization)
        ## Unique number of tokens
        tokens_count_unique = len(file_string_for_tokenization)

        t_time = dt.datetime.now()
        #SourcererCC formatting
        tokens = ','.join(['{}@@::@@{}'.format(k, v) for k,v in file_string_for_tokenization.iteritems()])
        t_time = (dt.datetime.now() - t_time).microseconds

        # MD5
        h_time = dt.datetime.now()
        m = hashlib.md5()
        m.update(tokens)
        hash_time = (dt.datetime.now() - h_time).microseconds

        final_tokens = (tokens_count_total,tokens_count_unique,m.hexdigest(),'@#@'+tokens)

        return (final_stats, final_tokens, [s_time, t_time, hash_time, re_time])

    def get_file_hash(self, file_string):
        m = hashlib.md5()
        m.update(file_string)
        return m.hexdigest()

    def process_file_contents(self, proj_id, file_string, file_path, file_bytes, FILE_tokens_file, db):
        self.process_logging.info('Starting file %s from project %s' % (file_path,proj_id, ))

        self.filecount += 1

        file_hash = self.get_file_hash(file_string)

        file_id = db.insert_file(proj_id,file_path,None,file_hash)

        file_parsing_times = [0,0,0,0]
        w_time = 0

        if not db.fileHash_exists(file_hash):
            # We checked before if the hash already exist to avoid the burden of tokenizing the file
            # Now we know it does not, we tokenize and send the info to the DB.
            # It might happen through cuncurrency that after our tokenization some other process
            # already inserted this key, but since these are defined as primary keys we will let
            # MySQL handle it
            
            (final_stats, final_tokens, file_parsing_times) = self.tokenize(file_string, self.comment_inline_pattern, self.comment_open_close_pattern, self.separators)
            (lines,LOC,SLOC) = final_stats
            (tokens_count_total,tokens_count_unique,token_hash,tokens) = final_tokens

            try:
                ww_time = dt.datetime.now()

                is_unique = db.insert_stats_and_is_tokenHash_unique(file_hash, file_bytes, str(lines), str(LOC), str(SLOC), str(tokens_count_total), str(tokens_count_unique), token_hash)

                if is_unique:
                    FILE_tokens_file.write(','.join([str(proj_id),str(file_id),str(tokens_count_total),str(tokens_count_unique),token_hash+tokens]) + '\n')
                    self.process_logging.info('File %s (%s) added to new input (its tokenHash is unique)' % (file_id,file_path))
                w_time = (dt.datetime.now() - ww_time).microseconds

            except Exception as e:
                self.process_logging.error('Error code 8374')

        return file_parsing_times + [w_time]

    def process_tgz_ball(self, process_num, tar_file, proj_path, proj_id, FILE_tokens_file, db):
        zip_time = file_time = string_time = tokens_time = hash_time = write_time = regex_time = 0

        try:
            with tarfile.open(tar_file,'r|*') as my_tar_file:

                for f in my_tar_file:
                    if not f.isfile():
                        continue

                    file_path = f.name
                    # Filter by the correct extension
                    if not os.path.splitext(f.name)[1] in self.file_extensions:
                        continue

                    # This is very strange, but I did find some paths with newlines,
                    # so I am simply ignoring them
                    if '\n' in file_path:
                        continue

                    file_bytes=str(f.size)

                    z_time = dt.datetime.now();
                    try:
                        myfile = my_tar_file.extractfile(f)
                    except:
                        self.process_logging.warning('Unable to open file (1) <'+os.path.join(tar_file,file_path)+'> (process '+str(process_num)+')')
                        break
                    zip_time += (dt.datetime.now() - z_time).microseconds

                    if myfile is None:
                        self.process_logging.warning('Unable to open file (2) <'+os.path.join(tar_file,file_path)+'> (process '+str(process_num)+')')
                        break

                    f_time      = dt.datetime.now()
                    file_string = myfile.read()
                    file_time   += (dt.datetime.now() - f_time).microseconds

                    times = self.process_file_contents(proj_id, file_string, file_path, file_bytes, FILE_tokens_file, db)
                    string_time += times[0]
                    tokens_time += times[1]
                    write_time  += times[4]
                    hash_time   += times[2]
                    regex_time  += times[3]

        except Exception as e:
            self.process_logging.warning('Unable to open tar on <'+proj_path+'> (process '+str(process_num)+')')
            self.process_logging.warning(e)
            return

        return (zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time)

    def process_zip_ball(self, process_num, proj_path, proj_id, FILE_tokens_file, db):
        zip_time = file_time = string_time = tokens_time = hash_time = write_time = regex_time = 0

        try:
            with zipfile.ZipFile(proj_path,'r') as my_zip_file:

                for file in my_zip_file.infolist():

                    if not os.path.splitext(file.filename)[1] in self.file_extensions:
                        continue

                    # This is very strange, but I did find some paths with newlines,
                    # so I am simply ignoring them
                    if '\n' in file.filename:
                        continue

                    file_bytes=str(file.file_size)

                    z_time = dt.datetime.now();
                    try:
                        myfile = my_zip_file.open(file.filename,'r')
                    except:
                        self.process_logging.warning('Unable to open file (1) <'+os.path.join(proj_path,file)+'> (process '+str(process_num)+')')
                        break
                    zip_time += (dt.datetime.now() - z_time).microseconds

                    if myfile is None:
                        self.process_logging.warning('Unable to open file (2) <'+os.path.join(proj_path,file)+'> (process '+str(process_num)+')')
                        break

                    f_time      = dt.datetime.now()
                    file_string = myfile.read()
                    file_time   += (dt.datetime.now() - f_time).microseconds

                    times = self.process_file_contents(proj_id, file_string, file.filename, file_bytes, FILE_tokens_file, db)
                    string_time += times[0]
                    tokens_time += times[1]
                    write_time  += times[4]
                    hash_time   += times[2]
                    regex_time  += times[3]

        except Exception as e:
            self.process_logging.warning('Unable to open zip on <'+proj_path+'> (process '+str(process_num)+')')
            self.process_logging.warning(e)
            return

        return (zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time)

    def process_one_project(self, process_num, proj_path, FILE_tokens_file, db):
        self.process_logging.info('Starting %s project <%s> (process %s)' % (self.PROJECTS_CONFIGURATION,proj_path,str(process_num)) )
        p_start = dt.datetime.now()

        if self.PROJECTS_CONFIGURATION == 'Leidos':
            if not os.path.isdir(proj_path):
                self.process_logging.warning('Unable to open %s project <%s> (process %s)' % (self.PROJECTS_CONFIGURATION,proj_path,str(process_num)))
            else:
                # Search for tar files with _code in them
                tar_files = [os.path.join(proj_path, f) for f in os.listdir(proj_path) if os.path.isfile(os.path.join(proj_path, f))]
                tar_files = [f for f in tar_files if '_code' in f]
                if(len(tar_files) != 1):
                    self.process_logging.warning('Tar not found on <'+proj_path+'> (process '+str(process_num)+')')
                else:
                    proj_id = db.insert_project(proj_path,None)

                    tar_file = tar_files[0]
                    times = self.process_tgz_ball(process_num, tar_file, proj_path, proj_id, FILE_tokens_file, db)
                    zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time = times

                    p_elapsed = dt.datetime.now() - p_start
                    self.process_logging.info('Project finished <%s,%s> (process %s)', proj_id, proj_path, process_num)
                    self.process_logging.info('Process (%s): Total: %smicros | Zip: %s Read: %s Separators: %smicros Tokens: %smicros Write: %smicros Hash: %s regex: %s', 
                        process_num,  p_elapsed, zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time)
        else:

            if self.PROJECTS_CONFIGURATION == 'GithubZIP':
                if not zipfile.is_zipfile(proj_path):
                    self.process_logging.warning('Unable to open %s project <%s> (process %s)' % (self.PROJECTS_CONFIGURATION,proj_path,str(process_num)))
                else:
                    proj_id = db.insert_project(proj_path,None)

                    times = self.process_zip_ball(process_num, proj_path, proj_id, FILE_tokens_file, db)
                    zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time = times

                    p_elapsed = dt.datetime.now() - p_start
                    self.process_logging.info('Project finished <%s,%s> (process %s)', proj_id, proj_path, process_num)
                    self.process_logging.info('Process (%s): Total: %smicros | Zip: %s Read: %s Separators: %smicros Tokens: %smicros Write: %smicros Hash: %s regex: %s', 
                        process_num,  p_elapsed, zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time)

            else:
                self.process_logging.error('Unknown project configuration format:%s' % (self.PROJECTS_CONFIGURATION))
                sys.exist(1)

    def process_projects(self, process_num, proj_paths, global_queue):
        db = DB(self.DB_user, self.DB_name, self.DB_pass, self.process_logging)
        try:
            FILE_files_tokens_file = os.path.join(self.output_folder,'files-tokens-'+str(process_num)+'.tokens')

            self.filecount = 0
            with open(FILE_files_tokens_file, 'a+') as FILE_tokens_file:
                p_start = dt.datetime.now()
                for proj_path in proj_paths:
                    self.process_one_project(process_num, proj_path, FILE_tokens_file, db)

                p_elapsed = (dt.datetime.now() - p_start).seconds
                self.process_logging.info('Process %s finished. %s files in %ss.', process_num, self.filecount, p_elapsed)

            # Let parent know
            global_queue.put((process_num, self.filecount))
            sys.exit(0)

        except Exception as e:
            self.process_logging.error('Error in process '+str(process_num))
            self.process_logging.error(e)
            sys.exit(1)

        finally:
            db.close()

    def start_child(self, processes, global_queue):
        # This is a blocking get. If the queue is empty, it waits
        pid, n_files_processed = global_queue.get()
        # OK, one of the processes finished. Let's get its data and kill it
        self.kill_child(processes, pid, n_files_processed)

        # Get a new batch of project paths ready
        paths_batch = self.proj_paths[:self.PROJECTS_BATCH]
        del self.proj_paths[:self.PROJECTS_BATCH]

        # Logging code
        self.process_logging = logging.getLogger('process_logging')
        FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
        formatter = logging.Formatter(fmt=FORMAT)
        handler = logging.FileHandler( os.path.join(self.logs_folder,'tokenizer_process_'+str(pid)+'.log') )
        handler.setFormatter(formatter)

        self.process_logging.addHandler(handler)
        self.process_logging.setLevel(logging.DEBUG)

        self.process_logging.propagate = False

        p = mp.Process(name='Process '+str(pid), target=self.process_projects, args=(pid, paths_batch, global_queue, ))
        processes[pid][0] = p
        p.start()

    def kill_child(self, processes, pid, n_files_processed):
        self.filecount += n_files_processed
        if processes[pid][0] != None:
            processes[pid][0] = None
            processes[pid][1] += n_files_processed
            
        self.controller_logging.info("Process %s finished, %s files processed (%s). Current total: %s" % (pid, n_files_processed, processes[pid][1], self.filecount))

        distinct_hash_tokens = 0
        output_tokens_file = os.path.join(self.output_folder,'files-tokens-'+str(pid)+'.tokens')
        if os.path.isfile(output_tokens_file):
            with open (output_tokens_file,'r') as process_result:
                for line in process_result:
                    distinct_hash_tokens += 1
        if distinct_hash_tokens > 0:
            self.controller_logging.info("Process %s finished, %s new distinct token-hashes processed" % (pid,distinct_hash_tokens))
        else:
            self.controller_logging.warning("Process %s finished but introduced zero new distinct token-hashes" % (pid))
            if os.path.isfile(output_tokens_file):
                os.remove(output_tokens_file)

    def active_process_count(self, processes):
        count = 0
        for p in processes:
            if p[0] != None:
                count +=1
        return count

    def execute(self):
        p_start = dt.datetime.now()

        # Multiprocessing with N_PROCESSES
        # [process, self.filecount]
        processes = [[None, 0] for i in xrange(self.N_PROCESSES)]
        # The queue for processes to communicate back to the parent (this process)
        # Initialize it with N_PROCESSES number of (process_id, n_files_processed)
        global_queue = mp.Manager().Queue()
        for i in xrange(self.N_PROCESSES):
            global_queue.put((i, 0))

        # Start all projects
        while len(self.proj_paths) > 0:
            self.start_child(processes, global_queue)

        #print "*** No more projects to process. Waiting for children to finish..."
        while self.active_process_count(processes) > 0:
            pid, n_files_processed = global_queue.get()
            self.kill_child(processes, pid, n_files_processed)

        p_elapsed = dt.datetime.now() - p_start
        self.controller_logging.info('Tokenizer finished. %s files in %s' % (self.filecount, p_elapsed))
