import logging
import multiprocessing as mp
from multiprocessing import Process, Value
import re
import os
import collections
from lockfile import LockFile
import tarfile
import sys
import hashlib

try:
  from configparser import ConfigParser
except ImportError:
  from ConfigParser import ConfigParser # ver. < 3.0

# Logging code
FORMAT = '[%(levelname)s] (%(threadName)s) %(message)s'
logging.basicConfig(level=logging.DEBUG,format=FORMAT)
file_handler = logging.FileHandler('LOG.log')
file_handler.setFormatter(logging.Formatter(FORMAT))
logging.getLogger().addHandler(file_handler)

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
FILE_projects_list = config.get('Main', 'FILE_projects_list')
PATH_stats_file_folder = config.get('Folders/Files', 'PATH_stats_file_folder')
PATH_bookkeeping_proj_folder = config.get('Folders/Files', 'PATH_bookkeeping_proj_folder')
PATH_tokens_file_folder = config.get('Folders/Files', 'PATH_tokens_file_folder')
    # Reading Language settings
separators = config.get('Language', 'separators').split(' ')
comment_end_of_line = config.get('Language', 'comment_inline')
comment_open_tag = re.escape(config.get('Language', 'comment_open_tag'))
comment_close_tag = re.escape(config.get('Language', 'comment_close_tag'))
file_extensions = config.get('Language', 'File_extensions').split(' ')

def get_proj_stats_helper(process_num, proj_id, proj_path, FILE_files_stats_file, FILE_bookkeeping_proj_name, FILE_files_tokens_file, file_starting_id):
  proj_path, proj_url = proj_path

  logging.info('Starting project <'+proj_id+','+proj_path+'> (process '+process_num+')')

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

      with file_starting_id.get_lock():
        all_files = zip(range(file_starting_id.value, len(all_files)+file_starting_id.value),all_files)
        file_starting_id.value = len(all_files)+file_starting_id.value

      for file_id, file_path in all_files:

        logging.info('Starting file <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'> (process '+process_num+')')

        file_bytes = 'ERROR'
        file_bytes=str(my_tar_file.getmember(file_path).size)

        try:
          myfile = my_tar_file.extractfile(file_path)
        except:
          logging.error('Unable to open file (1) <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'> (process '+process_num+')')
          break

        if myfile is None:
          logging.error('Unable to open file (2) <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'> (process '+process_num+')')
          break

        file_string = myfile.read()

        file_hash = 'ERROR'
        lines = 'ERROR'
        LOC = 'ERROR'
        SLOC = 'ERROR'
        file_url = proj_url + '/' + file_path[7:].replace(' ','%20')
        file_path = os.path.join(tar_file,file_path)

        m = hashlib.md5()
        m.update(file_string)
        file_hash = m.hexdigest()

        lines = str(file_string.count('\n'))
        file_string = os.linesep.join( [s for s in file_string.splitlines() if s] )
        LOC = str(file_string.count('\n'))

        # Remove tagged comments
        file_string = re.sub(re.escape('/*') + '.*?' + re.escape('*/'), '', file_string, flags=re.DOTALL)
        # Remove enf of line comments
        file_string = re.sub('//' + '.*?\n', '', file_string, flags=re.DOTALL)

        SLOC = str(file_string.count('\n'))

        with open(FILE_files_stats_file, 'a+') as FILE_stats_file:
          FILE_stats_file.write(','.join([proj_id,str(file_id),file_path,file_url,file_hash,file_bytes,lines,LOC,SLOC])+'\n')

        # Rather a copy of the file string here for tokenization
        file_string_for_tokenization = file_string

        #Transform separators into spaces (remove them)
        for x in separators:
          file_string_for_tokenization = file_string_for_tokenization.replace(x,' ')
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

        tokens = []
        #SourcererCC formatting
        for k, v in file_string_for_tokenization.items():
          tokens.append(k+'@@::@@'+str(v))
        tokens = ','.join(tokens)

        # MD5
        m = hashlib.md5()
        m.update(tokens)

        with open(FILE_files_tokens_file, 'a+') as FILE_tokens_file:
          FILE_tokens_file.write(','.join([proj_id,str(file_id),tokens_count_total,tokens_count_unique,m.hexdigest()+'@#@'+tokens+'\n']))

  except Exception:
    logging.error('Unable to open tar on <'+proj_id+','+proj_path+'> (process '+process_num+')')
    return

  with open(FILE_bookkeeping_proj_name, 'a+') as FILE_bookkeeping_proj:
    FILE_bookkeeping_proj.write(proj_id+','+proj_path+','+proj_url+'\n')

  logging.info('Project finished <'+proj_id+','+proj_path+'> (process '+process_num+')')

def get_project_stats(process_num,list_projects, FILE_files_stats_file, FILE_bookkeeping_proj_name, FILE_files_tokens_file, file_starting_id):
  for proj_id, proj_path in list_projects:
    get_proj_stats_helper(process_num, str(proj_id), proj_path, FILE_files_stats_file, FILE_bookkeeping_proj_name, FILE_files_tokens_file, file_starting_id)

if __name__ == '__main__':

  if os.path.exists(PATH_stats_file_folder) or os.path.exists(PATH_bookkeeping_proj_folder) or os.path.exists(PATH_tokens_file_folder):
    print 'ERROR - Folder ['+PATH_stats_file_folder+'] or ['+PATH_bookkeeping_proj_folder+'] or ['+PATH_tokens_file_folder+'] already exists!'
    sys.exit()
  else:
    os.makedirs(PATH_stats_file_folder)
    os.makedirs(PATH_bookkeeping_proj_folder)
    os.makedirs(PATH_tokens_file_folder)

  proj_paths = []
  with open(FILE_projects_list) as f:
    for line in f:
      line_split = line[:-1].split(',') # [:-1] to strip final character which is '\n'
      proj_paths.append((line_split[0],line_split[4]))

  proj_paths = zip(range(1, len(proj_paths)+1),proj_paths)

  #Split list of projects into N_PROCESSES lists
  proj_paths_list = [ proj_paths[i::N_PROCESSES] for i in xrange(N_PROCESSES) ]

  # Multiprocessing with N_PROCESSES
  processes = []
  # Multiprocessing shared variable instance for recording file_id
  file_starting_id = Value('i', 1)

  process_num = 0
  for input_process in proj_paths_list:

    # Skip empty sublists
    if len(input_process) == 0:
      continue

    process_num += 1
    FILE_files_stats_file = PATH_stats_file_folder+'/'+'files-stats-'+str(process_num)+'.txt'
    FILE_bookkeeping_proj_name = PATH_bookkeeping_proj_folder+'/'+'bookkeeping-proj-'+str(process_num)+'.txt'
    FILE_files_tokens_file = PATH_tokens_file_folder+'/'+'files-tokens-'+str(process_num)+'.txt'

    p = Process(name='Process '+str(process_num), target=get_project_stats, args=(str(process_num),input_process, FILE_files_stats_file, FILE_bookkeeping_proj_name, FILE_files_tokens_file, file_starting_id, ))
    processes.append(p)
    p.start()

