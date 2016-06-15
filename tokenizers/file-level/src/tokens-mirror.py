import logging
import multiprocessing as mp
from multiprocessing import Process
import re
import os
import collections
from lockfile import LockFile

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

config_file = 'config.ini'

# instantiate
config = ConfigParser()

# parse existing file
try:
	config.read(config_file)
except IOError:
	print 'config settings not fould'
	logging.error('Config file ['+config_file+'] not found')
	sys.exit()

# folders
PATH_tokens_folder = config.get('Folders/Files', 'PATH_tokens_folder')
PATH_bookkeeping_file_folder = config.get('Folders/Files', 'PATH_bookkeeping_file_folder')
PATH_bookkeeping_proj_folder = config.get('Folders/Files', 'PATH_bookkeeping_proj_folder')
PATH_TARGET = config.get('Folders/Files', 'PATH_mirror_repo')

token_files = [f for f in os.listdir(PATH_tokens_folder) if os.path.isfile(os.path.join(PATH_tokens_folder, f))]
book_proj_files = [f for f in os.listdir(PATH_bookkeeping_proj_folder) if os.path.isfile(os.path.join(PATH_bookkeeping_proj_folder, f))]
book_files_files = [f for f in os.listdir(PATH_bookkeeping_file_folder) if os.path.isfile(os.path.join(PATH_bookkeeping_file_folder, f))]

number = 0
while ('tokens_'+str(number)+'.txt') in token_files and ('bookkeeping_proj_'+str(number)+'.txt') in book_proj_files and ('bookkeeping_file_'+str(number)+'.txt') in book_files_files:
	with open(os.path.join(PATH_tokens_folder,'tokens_'+str(number)+'.txt'),'r') as tokens, open(os.path.join(PATH_bookkeeping_proj_folder,'bookkeeping_proj_'+str(number)+'.txt'),'r') as projects, open(os.path.join(PATH_bookkeeping_file_folder,'bookkeeping_file_'+str(number)+'.txt'),'r') as files:

		print 'Reading ','bookkeeping_file_'+str(number)+'.txt'
		files_dict = {}
		for file in files:
			if len(file) > 2:
				file = file.strip('\n').split(',')
				# Derive file name from file path and map it to (proj_id, file_id)
				files_dict[(file[0], file[1])] = file[2]

		print 'Reading ','bookkeeping_proj_'+str(number)+'.txt'
		projs_dict = {}
		for project in projects:
			if len(project) > 2:
				project = project.strip('\n').split(',')
				# Get project path name
				projs_dict[project[0]] = project[1]
				# Create folder if does not exist
				if not os.path.exists(os.path.join(PATH_TARGET,project[1])):
					os.makedirs(os.path.join(PATH_TARGET,project[1]))

		print 'Reading ','tokens_'+str(number)+'.txt'
		for token in tokens:
			# Proceed if token file is not empty
			if len(token) > 2:
				# Write the mirror to corresponding file
				with open(os.path.join(PATH_TARGET,projs_dict[token.split(',')[0]])+'/tokens.txt','a') as tokens_file:
					file_path = files_dict[(token.split(',')[0],token.split(',')[1])].split(projs_dict[token.split(',')[0]])[1][1:]
					# print file_path
					tokens_file.write(file_path+'@#@'+token.split('@#@')[1])

	number +=1
