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

class Tokenizer(object):
    project_id = 1
    proj_paths = []
    filecount = 0
    logs_folder = '' # This is different from the 'overall' log of the tokenizer. This is for each individual log for each process
    output_folder = '' # This output will be the input of CC
    PATH_logs = 'logs'
    PATH_output = 'output'
    PATH_config_file = 'config.ini'

    def __init__(self, path_list_projects, DB_user, DB_pass, DB_name, logging, target_folders):

        self.N_PROCESSES = 2
        self.PROJECTS_BATCH = 2

        self.target_folders = target_folders
        self.DB_user = DB_user
        self.DB_pass = DB_pass
        self.DB_name = DB_name

        try:
            db = MySQLdb.connect(host="localhost", # your host, usually localhost
                                 db=DB_name,
                                 user=DB_user,     # your username
                                 passwd=DB_pass)   # your password
            cursor = db.cursor()

            cursor.execute("SELECT Max(projectId) FROM projects;")
            (self.project_id, ) = cursor.fetchone()
            if self.project_id is None:
                self.project_id = 0
    
            self.project_id += 1

        except Exception as e:
            print 'Error on Tokenizer.__init__'
            print e
            sys.exit(1)

        finally:
            cursor.close()
            db.close()

        self.proj_paths = []
        with open(path_list_projects) as f:
            for line in f:
                self.proj_paths.append( line[:-1] )
        self.proj_paths = zip(range(self.project_id, self.project_id+len(self.proj_paths)+1), self.proj_paths)

        # Creating folder for the processes logs
        self.logs_folder   = os.path.join(self.PATH_logs,self.target_folders)
        if os.path.exists( self.logs_folder ):
            logging.error('ERROR - Folder [%s] already exists!' % self.logs_folder )
            sys.exit(1)
        else:
            os.makedirs(self.logs_folder)

        # Create folder for processes output
        self.output_folder = os.path.join(self.PATH_output,self.target_folders)
        if os.path.exists( self.output_folder ):
            logging.error('ERROR - Folder [%s] already exists!' % self.output_folder )
            sys.exit(1)
        else:
            os.makedirs(self.output_folder)

        # Reading config file
        config = ConfigParser.ConfigParser()

        try:
            config.read(self.PATH_config_file)
        except Exception as e:
            logging.error('ERROR on Tokenizer.__init__')
            logging.error(e)
            sys.exit(1)

        comment_inline         = re.escape(config.get('Language', 'comment_inline'))
        comment_open_tag       = re.escape(config.get('Language', 'comment_open_tag'))
        comment_close_tag      = re.escape(config.get('Language', 'comment_close_tag'))
        self.separators             = config.get('Language', 'separators').strip('"').split(' ')
        self.comment_inline_pattern = comment_inline + '.*?$'
        self.comment_open_close_pattern = comment_open_tag + '.*?' + comment_close_tag
        self.file_extensions        = config.get('Language', 'File_extensions').split(' ')


        logging.info('Tokenizer successfully initialized. Project index starting at %s. Processing %s projects. Looking for file extensions: %s' % (self.project_id, len(self.proj_paths), self.file_extensions) )


    def tokenize(file_string, comment_inline_pattern, comment_open_close_pattern, separators):
        print 'tokenize'

    def get_file_hash(self, file_string):
        m = hashlib.md5()
        m.update(file_string)
        return m.hexdigest()

    # ALL strings that are sent to the DB must be sanitized first
    # Not really all of them, but the ones there no control over (paths, url's, names, etc)
    def sanitize_strings(self, string):
        return (string.replace('\"','\'\''))[:4000]

    def process_file_contents(self, proj_id, file_string, file_path, file_bytes, FILE_tokens_file, db):
        self.filecount += 1

        file_hash = self.get_file_hash(file_string)

        cursor = db.cursor()
        q = "INSERT INTO files VALUES (NULL, %s, \"%s\", NULL, \"%s\");" % (proj_id, file_path, file_hash)
        cursor.execute(q)
        cursor.close()

        exists = 0
        cursor = db.cursor()
        cursor.execute("SELECT COUNT(*) FROM stats WHERE fileHash = '"+file_hash+"';")
        (exists, ) = cursor.fetchone()
        cursor.close()

        if exists == 0:
            p = 'exists'
            # Insert stats, make sure the query itself precents repetition of token hash
            # write entry into output file


        #    (final_stats, final_tokens, file_parsing_times) = self.tokenize(file_string, comment_inline_pattern, comment_open_close_pattern, separators)
        #    (file_hash,lines,LOC,SLOC) = final_stats
        #    (tokens_count_total,tokens_count_unique,token_hash,tokens) = final_tokens

        #    ww_time = dt.datetime.now()
        #    q = """INSERT INTO filesStats (fileId, projectId, fileLeidosPath, fileUrl, fileHash, fileBytes, fileLines, fileLOC, fileSLOC, totalTokens, uniqueTokens, tokenHash) VALUES (NULL, %s, '%s', '%s', '%s', %s, %s, %s, %s, %s, %s, '%s'); SELECT LAST_INSERT_ID();""" % (proj_id, file_path, file_url, file_hash, file_bytes, str(lines), str(LOC), str(SLOC), str(tokens_count_total), str(tokens_count_unique), token_hash) 
        #    cursor = db.cursor()
        #    cursor.execute(q)
        #    cursor.close()
        #    file_id = cursor.lastrowid
        #    #print '# MySQL stats'+(','.join([proj_id,str(file_id),'\"'+file_path+'\"','\"'+file_url+'\"','\"'+file_hash+'\"',file_bytes,str(lines),str(LOC),str(SLOC)]) + '\n')
        #    w_time = (dt.datetime.now() - ww_time).microseconds

        #    ww_time = dt.datetime.now()
        #    FILE_tokens_file.write(','.join([proj_id,str(file_id),str(tokens_count_total),str(tokens_count_unique),token_hash+tokens]) + '\n')
        #    w_time += (dt.datetime.now() - ww_time).microseconds
        #    return file_parsing_times + [w_time] # [s_time, t_time, w_time, hash_time, re_time]
        #else:
        #    q = """INSERT INTO filesStats (fileId, projectId, fileLeidosPath, fileUrl, fileHash, fileBytes, fileLines, fileLOC, fileSLOC, totalTokens, uniqueTokens, tokenHash) VALUES (NULL, %s, '%s', '%s', '%s', NULL, NULL, NULL, NULL, NULL, NULL, NULL);""" % (proj_id, file_path, file_url, file_hash) 
        #    cursor = db.cursor()
        #    cursor.execute(q)
        #    cursor.close()
        #    return [0,0,0,0,0,]

# file_hash = entry[4]
# q = "SELECT COUNT(*) FROM stats WHERE fileHash = '"+file_hash+"';"
# #print q
# cursor.execute(q)
# (exists, ) = cursor.fetchone()

# if exists == 0:
#     q = "INSERT INTO stats VALUES (\"%s\", %s, %s, %s, %s, %s, %s, \"%s\");" % (file_hash,entry[5],entry[6],entry[7],entry[8],token_info[entry[1]][0],token_info[entry[1]][1],token_info[entry[1]][2])
#     #print q
#     cursor.execute(q)


        return [1,2,3,4,5]

    def process_tgz_ball(self, process_num, tar_file, proj_path, proj_id, FILE_tokens_file, logging, db):
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
                        logging.error('Unable to open file (1) <'+os.path.join(tar_file,file_path)+'> (process '+str(process_num)+')')
                        break
                    zip_time += (dt.datetime.now() - z_time).microseconds

                    if myfile is None:
                        logging.error('Unable to open file (2) <'+os.path.join(tar_file,file_path)+'> (process '+str(process_num)+')')
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
            logging.error('Unable to open tar on <'+proj_path+'> (process '+str(process_num)+')')
            logging.error(e)
            return

        return (zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time)


    def process_one_project(self, process_num, proj_path, proj_id, FILE_tokens_file, logging, db):
        logging.info('Starting project <'+proj_path+'> (process '+str(process_num)+')')
        p_start = dt.datetime.now()

        if not os.path.isdir(proj_path):
            logging.error('Unable to open project <'+proj_path+'> (process '+str(process_num)+')')
            return

        # Search for tar files with _code in them
        tar_files = [os.path.join(proj_path, f) for f in os.listdir(proj_path) if os.path.isfile(os.path.join(proj_path, f))]
        tar_files = [f for f in tar_files if '_code' in f]
        if(len(tar_files) != 1):
            logging.error('Tar not found on <'+proj_path+'> (process '+str(process_num)+')')
        else:
            tar_file = tar_files[0]
            times = self.process_tgz_ball(process_num, tar_file, proj_path, proj_id, FILE_tokens_file, logging, db)
            #zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time = times

            cursor = db.cursor()
            cursor.execute("""INSERT INTO projects VALUES (%s, \"%s\", NULL);""", (proj_id,self.sanitize_strings(proj_path)) )
            cursor.close()

            p_elapsed = dt.datetime.now() - p_start
            logging.info('Project finished <%s,%s> (process %s)', proj_id, proj_path, process_num)
            #logging.info(' (%s): Total: %smicros | Zip: %s Read: %s Separators: %smicros Tokens: %smicros Write: %smicros Hash: %s regex: %s', 
            #    process_num,  p_elapsed, zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time)



    def process_projects(self, process_num, proj_paths, global_queue):
        # Logging code

        FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
        logging.basicConfig(level=logging.DEBUG,format=FORMAT)
        file_handler = logging.FileHandler( os.path.join(self.logs_folder,str(process_num)+'.log') )
        file_handler.setFormatter(logging.Formatter(FORMAT))
        logging.getLogger().addHandler(file_handler)

        try:
            db = MySQLdb.connect(host   = "localhost", # your host, usually localhost
                                 db     = self.DB_name,
                                 user   = self.DB_user,     # your username
                                 passwd = self.DB_pass)   # your password

            FILE_files_tokens_file = os.path.join(self.output_folder,'files-tokens-'+str(process_num)+'.tokens')

            self.filecount = 0
            with open(FILE_files_tokens_file, 'a+') as FILE_tokens_file:
                logging.info("Process %s starting", process_num)
                p_start = dt.datetime.now()
                for proj_id, proj_path in proj_paths:
                    self.process_one_project(process_num, proj_path, str(proj_id), FILE_tokens_file, logging, db)

                p_elapsed = (dt.datetime.now() - p_start).seconds
                logging.info('Process %s finished. %s files in %ss.', process_num, self.filecount, p_elapsed)

            # Let parent know
            global_queue.put((process_num, self.filecount))
            sys.exit(0)

        except Exception as e:
            logging.error('Error in process '+str(process_num))
            logging.error(e)
            sys.exit(1)

        finally:
            db.commit()
            db.close()

    def start_child(self, processes, global_queue, proj_paths):
        # This is a blocking get. If the queue is empty, it waits
        pid, n_files_processed = global_queue.get()
        # OK, one of the processes finished. Let's get its data and kill it
        self.kill_child(processes, pid, n_files_processed)

        # Get a new batch of project paths ready
        paths_batch = self.proj_paths[:self.PROJECTS_BATCH]
        del self.proj_paths[:self.PROJECTS_BATCH]

        p = mp.Process(name='Process '+str(pid), target=self.process_projects, args=(pid, paths_batch, global_queue, ))
        processes[pid][0] = p
        p.start()

    def kill_child(self, processes, pid, n_files_processed):
        self.filecount += n_files_processed
        if processes[pid][0] != None:
            processes[pid][0] = None
            processes[pid][1] += n_files_processed
            
        logging.info("Process %s finished, %s files processed (%s). Current total: %s" % (pid, n_files_processed, processes[pid][1], self.filecount))
    
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
            self.start_child(processes, global_queue, self.proj_paths)

        #print "*** No more projects to process. Waiting for children to finish..."
        while self.active_process_count(processes) > 0:
            pid, n_files_processed = global_queue.get()
            self.kill_child(processes, pid, n_files_processed)

        p_elapsed = dt.datetime.now() - p_start
        logging.info('Tokenizer finished. %s files in %s' % (self.filecount, p_elapsed))

