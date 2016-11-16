import sys, os
import logging
import datetime
import MySQLdb
import time
import subprocess
import csv

try:
    from configparser import ConfigParser
except ImportError:
    from ConfigParser import ConfigParser # ver. < 3.0

PATH_logs = 'logs'
PATH_tokenizer = os.path.join('bin','tokenizer')
PATH_CC_input = ''
DB_user = ''
DB_pass = ''
DB_name = ''

def read_config(logging):
    global DB_user, DB_pass, DB_name

    # instantiate
    config = ConfigParser()

    # parse existing file
    try:
        config.read('config.ini')
    except IOError:
        logging.error('ERROR - Config settings not found')
        sys.exit()

    # Get info from config.ini into global variables
    DB_user = config.get('MySQL', 'User')
    DB_pass = config.get('MySQL', 'Pass')
    DB_name = config.get('MySQL', 'Name')

    logging.info('Config file successfully read')

def db_connect(logging):
    try:
        db = MySQLdb.connect(host="localhost", # your host, usually localhost
                             user=DB_user,     # your username
                             passwd=DB_pass)   # your password
        cursor = db.cursor()
        cursor.execute('CREATE DATABASE IF NOT EXISTS %s;' % DB_name)
        db.commit()
        db.close()

        db = MySQLdb.connect(host="localhost", # your host, usually localhost
                             db=DB_name,
                             user=DB_user,     # your username
                             passwd=DB_pass)   # your password
        cursor = db.cursor()

        projectsTable = """ CREATE TABLE IF NOT EXISTS `projects` (
                           projectId INT(15) UNSIGNED PRIMARY KEY,
                           projectLeidosPath VARCHAR(3000) NOT NULL,
                           projectUrl VARCHAR(3000) NOT NULL
                           ) ENGINE = MYISAM; """
        cursor.execute(projectsTable)
        db.commit()
        
        filesStatsTable = """ CREATE TABLE IF NOT EXISTS `filesStats` (
                                   fileId BIGINT(10) UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                                   projectId INT(10) UNSIGNED NOT NULL,
                                   fileLeidosPath VARCHAR(3000) NOT NULL,
                                   fileUrl VARCHAR(3000) NULL,
                                   fileHash VARCHAR(32) NULL,
                                   fileBytes INT(7) UNSIGNED NULL,
                                   fileLines INT(7) UNSIGNED NULL,
                                   fileLOC INT(7) UNSIGNED NULL,
                                   fileSLOC INT(7) UNSIGNED NULL,
                                   totalTokens INT(7) UNSIGNED NULL,
                                   uniqueTokens INT(7) UNSIGNED NULL,
                                   tokenHash VARCHAR(32) NULL,
                                   INDEX (projectId),
                                   INDEX (fileHash),
                                   INDEX (tokenHash)
                                   ) ENGINE = MYISAM; """
        cursor.execute(filesStatsTable)
        db.commit()

    except Exception as e:
        logging.error('Error connecting to MySQL with credentials \''+DB_user+':'+DB_pass+'\'@\'localhost\'')
        logging.error(e)
        sys.exit(1)

    finally:
        db.close()

    logging.info('Connection to database \''+DB_name+'\' successful')
    logging.info('Database \''+DB_name+'\' successfully initialized')

def tokenize(logging):
    logging.info('Starting tokenizer')

    tokenizer_config_file = os.path.join(PATH_tokenizer,'config.ini')

    logging.info('Editing tokenizer config file in \''+tokenizer_config_file+'\'')
    # instantiate
    config = ConfigParser()
    config.optionxform = lambda option: option

    # parse existing file
    try:
        config.read(tokenizer_config_file)
    except Exception as e:
        logging.error('Config settings for tokenizer not found')
        logging.error(e)
        sys.exit()

    config.set('Main','N_PROCESSES','38')
    config.set('Main','FILE_projects_list',sys.argv[1])

    output_folder = os.path.join(PATH_tokenizer, 'output', str(time.time()) )

    config.set( 'Folders/Files', 'PATH_tokens_file_folder', os.path.join(output_folder,'files_tokens') )
    config.set( 'Folders/Files', 'PATH_logs',               os.path.join(output_folder,'logs') )

    config.set('MySQL', 'User', DB_user)
    config.set('MySQL', 'Pass', DB_pass)
    config.set('MySQL', 'Name', DB_name)

    with open(PATH_tokenizer+'/config.ini','w') as new_config:
        config.write(new_config)

    logging.info('Starting tokenizer, output in \''+output_folder+'\'')

    try:
        #subprocess.check_output('python '+os.path.join(PATH_tokenizer,'tokenizer.py'), shell=True)
        subprocess.call('python '+os.path.join(PATH_tokenizer,'tokenizer.py'), shell=True)
        print 'python '+os.path.join(PATH_tokenizer,'tokenizer.py')
    except Exception as e:
        logging.error('An error occured on the tokenizer')
        logging.error(e)

    logging.info('Tokenizer finished')

    return output_folder

def generate_CC_input(init_file_id,logging):
    logging.info('Generating input to CC')
    file_ids = set()

    try:
        db = MySQLdb.connect(host="localhost", # your host, usually localhost
                             db=DB_name,
                             user=DB_user,     # your username
                             passwd=DB_pass)   # your password
        cursor = db.cursor()

        print 'here'

        cursor.execute("SELECT min(fileId) FROM filesTokens GROUP BY tokenHash;")

        for row in cursor:
            print row
        
        #    if row[0] != None:
        #        # This if is just to get only the new ids.
        #        if row[0] <= init_file_id:
        #            file_ids.add(str(row[0]))

        logging.info('A total of '+str(len(file_ids))+' distinct token hashes were found')

    except Exception as e:
        logging.error('Error connecting to MySQL with credentials \''+DB_user+':'+DB_pass+'\'@\'localhost\'')
        logging.error(e)
        sys.exit(1)

    finally:
        db.close()

def run_SourcererCC(tokenized_output):
    print 'running CC, input in',tokenized_output

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print 'No arguments given. Usage: $pythoon this_script.py list-of-projects-folders.txt'
        sys.exit(1)

    # Logging code
    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.INFO,format=FORMAT)
    file_handler = logging.FileHandler( os.path.join( PATH_logs,str(time.time() ) )+'.log')
    file_handler.setFormatter(logging.Formatter(FORMAT))
    logging.getLogger().addHandler(file_handler)

    logging.info('Starting')

    read_config(logging)
    db_connect(logging)
    tokenized_output = tokenize(logging)
    run_SourcererCC(tokenized_output)
