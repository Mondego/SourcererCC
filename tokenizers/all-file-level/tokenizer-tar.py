import logging
import multiprocessing as mp
from multiprocessing import Process
import re
import os
import collections
from lockfile import LockFile
import tarfile
import mimetypes
import sys
import hashlib

try:
	from configparser import ConfigParser
except ImportError:
	from ConfigParser import ConfigParser # ver. < 3.0

# Logging code
FORMAT = '[%(levelname)s] (%(threadName)s) %(message)s'
logging.basicConfig(level=logging.DEBUG,format=FORMAT)
file_handler = logging.FileHandler('results.log')
file_handler.setFormatter(logging.Formatter(FORMAT))
logging.getLogger().addHandler(file_handler)

'''
	Command line argument config file name:  config.ini
	for default setting use '-d'
'''
args = []
config_file = 'config-tar.ini'
if len(sys.argv) > 1:
	args = sys.argv[1:]
	if args[0] != '-d':
		if os.path.isfile(args[0]):
			config_file = args[0]
		else:
			print 'File Not Exist'
			logging.error('Config file ['+args[0]+'] not found')
			sys.exit()

# instantiate
config = ConfigParser()

# parse existing file
try:
	config.read(config_file)
except IOError:
	print 'config settings not fould'
	logging.error('Config file ['+config_file+'] not found')
	sys.exit()

# Provided by the user
# if len(args) == 3:
# 	if args[0] != '-d':
# 		if os.path.isfile(args[0]):
# 			PATH_proj_paths = args[0]
# 		else:
# 			print 'File Not Exist'
# 			sys.exit()
# 	else:
# 		PATH_proj_paths = config.get('Main', 'PATH_proj_paths')

# 	if args[1] != '-d':
# 		try: 
# 			n = int(args[1])
# 			if n > 0:
# 				N_PROCESSES = n
# 			else:
# 				print 'Invalid number of process'
# 				sys.exit()
# 		except ValueError:
# 			print 'Invalid number of process'
# 			sys.exit()
# 	else:
# 		N_PROCESSES = config.getint('Main', 'N_PROCESSES')

# 	if args[2] != '-d':
# 		if args[2].isalpha():
# 			language = args[2]
# 		else:
# 			print 'Invalid language name'
# 			sys.exit()
# 	else:
# 		language = config.get('Main', 'language')
# else:
# 	print 'Invalid commands combination, use default settings'

PATH_proj_paths = config.get('Main', 'PATH_proj_paths')
N_PROCESSES = config.getint('Main', 'N_PROCESSES')
language_file = config.get('Main', 'language_file')

# Folders
PATH_tokens_folder = config.get('Folders/Files', 'PATH_tokens_folder')
PATH_bookkeeping_file_folder = config.get('Folders/Files', 'PATH_bookkeeping_file_folder')
PATH_bookkeeping_proj_folder = config.get('Folders/Files', 'PATH_bookkeeping_proj_folder')
PATH_projects_success = config.get('Folders/Files', 'PATH_projects_success')
PATH_project_starting_index = config.get('Folders/Files', 'PATH_project_starting_index')
PATH_projects_fail = config.get('Folders/Files', 'PATH_projects_fail')

try:
	config.read(language_file)
except IOError:
	print 'Language settings not fould'
	logging.error('Language settings ['+language_file+'] not found')
	sys.exit()

# Read language settings
separators = config.get('Language', 'separators').split(', ')
file_extensions = config.get('Language', 'file_extensions').split(', ')
comment_end_of_line = config.get('Language', 'comment_inline')
comment_open_tag = re.escape(config.get('Language', 'comment_open_tag'))
comment_close_tag = re.escape(config.get('Language', 'comment_close_tag'))


ALWAYS = ['@','@#@','@@::@@','#'] # These should be always part of the separators
separators.extend(ALWAYS)

# Some of the files we found happen to be binary, even if we their extension is something
# line *.cpp. Therefore we explore a behavior of file(1) to find if these files are binary
# http://stackoverflow.com/questions/32184809/python-file1-why-are-the-numbers-7-8-9-10-12-13-27-and-range0x20-0x100
textchars = bytearray({7,8,9,10,12,13,27} | set(range(0x20, 0x100)) - {0x7f})
is_binary_string = lambda bytes: bool(bytes.translate(None, textchars))

def tokenizer(proj_id, proj_path, FILE_tokens_name, FILE_bookkeeping_file_name, FILE_bookkeeping_proj_name):
	logging.info('Starting project <'+proj_id+','+proj_path+'>')

	if not os.path.isdir(proj_path):
		logging.error('Unable to open project <'+proj_id+','+proj_path+'>')
		lock = LockFile(PATH_projects_fail)
		with lock:
			with open(PATH_projects_fail,'a+') as project_failure:
				project_failure.write(proj_path+'\n')
		return

	# Search for all tar files
	tar_files = [os.path.join(proj_path, f) for f in os.listdir(proj_path) if os.path.isfile(os.path.join(proj_path, f))]
	tar_files = [f for f in tar_files if '_code' in f]
	if(len(tar_files) != 1):
		logging.error('Tar not found on <'+proj_id+','+proj_path+'>')
		# Important to have a global loc on this file because it is shared
		lock = LockFile(PATH_projects_fail)
		with lock:
			with open(PATH_projects_fail,'a+') as project_fail:
				project_fail.write(proj_path+'\n')
		return

	tar_file = tar_files[0]

	try:
		with tarfile.open(tar_file,'r') as my_tar_file:
			# Get all members on the tar file
			all_files = []
			for member in my_tar_file.getmembers():
				all_files.append(member.name)

			print all_files

			# Filter them by the correct extension
			aux = []
			for extension in file_extensions:
				aux.extend([x for x in all_files if x.endswith(extension)])
			all_files = aux

			# This is very strange, but I did find some paths with newlines,
			# so I am simply eliminatins these
			all_files = [x for x in all_files if '\n' not in x]

			# In case process names need to be logged
			# process_name = '['+mp.current_process().name+'] '

			all_files = zip(range(0,len(all_files)),all_files)

			for file_id, file_path in all_files:

				logging.info('Starting file <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'>')

				try:
					myfile = my_tar_file.extractfile(file_path)
				except:
					logging.error('Unable to open file (1) <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'>')
					break

				if myfile is None:
					logging.error('Unable to open file (2) <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'>')
					break

				file_string = myfile.read()

				if is_binary_string(file_string):
					logging.error('Unable to open file (3) <'+proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'>')
					break

				# Remove enf of line comments
				file_string = re.sub(comment_end_of_line+'.*?\n','',file_string,flags=re.DOTALL)
				# Remove tagged comments
				file_string = re.sub(comment_open_tag+'.*?'+comment_close_tag,'',file_string,flags=re.DOTALL)
				#Transform separators into spaces (remove them)
				for x in separators:
					file_string = file_string.replace(x,' ')
				#Create a list of tokens
				file_string = file_string.split()
				# Total number of tokens
				tokens_count_total = len(file_string)
				#Count occurrences
				file_string = collections.Counter(file_string)
				#Converting Counter to dict because according to StackOverflow is better
				file_string=dict(file_string)
				# Unique number of tokens
				tokens_count_unique = len(file_string)

				tokens = []
				#SourcererCC formatting
				for k, v in file_string.items():
					tokens.append(k+'@@::@@'+str(v))
				tokens = ','.join(tokens)

				# MD5
				m = hashlib.md5()
				m.update(tokens)

				with open(FILE_tokens_name, 'a+') as FILE_tokens_file:
					FILE_tokens_file.write(proj_id+','+str(file_id)+','+str(tokens_count_total)+','+str(tokens_count_unique)+','+m.digest()+'@#@'+tokens+'\n')

				with open(FILE_bookkeeping_file_name, 'a+') as FILE_bookkeeping_file:
					FILE_bookkeeping_file.write(proj_id+','+str(file_id)+','+os.path.join(tar_file,file_path)+'\n')

	except Exception:
		logging.error('Unable to open tar on <'+proj_id+','+proj_path+'>')
		lock = LockFile(PATH_projects_fail)
		with lock:
			with open(PATH_projects_fail,'a+') as project_failure:
				project_failure.write(proj_path+'\n')
		return

	with open(FILE_bookkeeping_proj_name, 'a+') as FILE_bookkeeping_proj:
		FILE_bookkeeping_proj.write(proj_id+','+proj_path+'\n')

	# Important to have a global loc on this file because it is shared
	lock = LockFile(PATH_projects_success)
	with lock:
		with open(PATH_projects_success,'a+') as project_success:
			project_success.write(proj_path+'\n')

	logging.info('Project finished <'+proj_id+','+proj_path+'>')


def tokenize(list_projects, FILE_tokens_name, FILE_bookkeeping_file_name, FILE_bookkeeping_proj_name):

	# Each tokenize will represent a new process
	for proj_id, proj_path in list_projects:
		tokenizer(str(proj_id), proj_path, FILE_tokens_name, FILE_bookkeeping_file_name, FILE_bookkeeping_proj_name)


if __name__ == '__main__':
	#In the main file we:
	#       create directories if they do not exist
	#       read list of PATH_projects_success, if exists, and do not process these again
	#       each process needs a unique file with tokens and file and project
	#               bookkeeping in the proper folders
	#       start N_PROCESSES, and give them [(unique_id, proj_path)]

	if not os.path.exists(PATH_tokens_folder):
		os.makedirs(PATH_tokens_folder)
	if not os.path.exists(PATH_bookkeeping_file_folder):
		os.makedirs(PATH_bookkeeping_file_folder)
	if not os.path.exists(PATH_bookkeeping_proj_folder):
		os.makedirs(PATH_bookkeeping_proj_folder)

	proj_paths = []
	with open(PATH_proj_paths) as f:
		for line in f:
			proj_paths.append(line.strip('\n'))

	projects_success = []
	try:
		with open(PATH_projects_success,'r') as f:
			for line in f:
				projects_success.append(line.strip().strip('\n'))
	except IOError as e:
		logging.info('File '+PATH_projects_success+' no found')

	projects_starting_index = 0
	proj_paths = list(set(proj_paths) - set(projects_success))

	# Initialize projects_starting_index with previous logged number
	if not os.path.exists(PATH_project_starting_index):
		with open(PATH_project_starting_index, 'w') as FILE_project_starting_index:
			FILE_project_starting_index.write(str(len(proj_paths))+'\n')
	else:
		try:
			with open(PATH_project_starting_index, 'r') as FILE_project_starting_index:
					projects_starting_index = int(FILE_project_starting_index.readline().strip('\n'))
		except ValueError:
			projects_starting_index = 0

		with open(PATH_project_starting_index, 'w') as FILE_project_starting_index:
			FILE_project_starting_index.write(str(projects_starting_index+len(proj_paths))+'\n')

	proj_paths = zip(range(projects_starting_index, len(proj_paths)+projects_starting_index),proj_paths)
#       print proj_paths

	#Split list of projects into N_PROCESSES lists
	proj_paths_list = [ proj_paths[i::N_PROCESSES] for i in xrange(N_PROCESSES) ]
	# print proj_paths_list
	# Multiprocessing with N_PROCESSES
	processes = []
	process_num = 0
	n =0
	for input_process in proj_paths_list:

		# Skip empty sublists
		if len(input_process) == 0:
			continue

		process_num += 1
		FILE_tokens_name = PATH_tokens_folder+'/'+'tokens_'+str(n)+'.txt'
		FILE_bookkeeping_file_name = PATH_bookkeeping_file_folder+'/'+'bookkeeping_file_'+str(n)+'.txt'
		FILE_bookkeeping_proj_name = PATH_bookkeeping_proj_folder+'/'+'bookkeeping_proj_'+str(n)+'.txt'

		while (os.path.isfile(FILE_tokens_name) and os.path.isfile(FILE_bookkeeping_file_name) and os.path.isfile(FILE_bookkeeping_proj_name)):
			n += 1
			FILE_tokens_name = PATH_tokens_folder+'/'+'tokens_'+str(n)+'.txt'
			FILE_bookkeeping_file_name = PATH_bookkeeping_file_folder+'/'+'bookkeeping_file_'+str(n)+'.txt'
			FILE_bookkeeping_proj_name = PATH_bookkeeping_proj_folder+'/'+'bookkeeping_proj_'+str(n)+'.txt'

		n += 1
		processes.append(Process(name='Process '+str(process_num), target=tokenize, args=(input_process, FILE_tokens_name, FILE_bookkeeping_file_name, FILE_bookkeeping_proj_name,)))

	for proc in processes:
		proc.start()
		logging.info(proc.name)

	for proc in processes:
		proc.join()
