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

PATH_logs = os.path.join('logs','tokenizer')
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

        cursor.execute("DROP TABLE IF EXISTS `projects`;")
        table = """ CREATE TABLE IF NOT EXISTS `projects` (
                       projectId   INT(6)        UNSIGNED PRIMARY KEY,
                       projectPath VARCHAR(4000)          NULL,
                       projectUrl  VARCHAR(4000)          NOT NULL
                       ) ENGINE = MYISAM; """
        cursor.execute(table)
        db.commit()
    
        cursor.execute("DROP TABLE IF EXISTS `files`;")
        table = """CREATE TABLE IF NOT EXISTS `files` (
                       fileId       BIGINT(6)     UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                       projectId    INT(6)        UNSIGNED NOT NULL,
                       relativePath VARCHAR(4000)          NULL,
                       relativeUrl  VARCHAR(4000)          NOT NULL,
                       fileHash     CHAR(32)               NOT NULL,
                       INDEX (projectId),
                       INDEX (fileHash)
                       ) ENGINE = MYISAM;"""
        cursor.execute(table)
        db.commit()
    
        cursor.execute("DROP TABLE IF EXISTS `stats`;")
        table = """CREATE TABLE IF NOT EXISTS `stats` (
                       fileHash     CHAR(32)        PRIMARY KEY,
                       fileBytes    INT(6) UNSIGNED NOT NULL,
                       fileLines    INT(6) UNSIGNED NOT NULL,
                       fileLOC      INT(6) UNSIGNED NOT NULL,
                       fileSLOC     INT(6) UNSIGNED NOT NULL,
                       totalTokens  INT(6) UNSIGNED NOT NULL,
                       uniqueTokens INT(6) UNSIGNED NOT NULL,
                       tokenHash    CHAR(32)        NOT NULL,
                       INDEX (tokenHash)
                       ) ENGINE = MYISAM;"""
        cursor.execute(table)
        db.commit()
    
        cursor.execute("DROP TABLE IF EXISTS `CCPairs`;")
        table = """CREATE TABLE `CCPairs` (
                       projectId1 INT(6) NOT NULL,
                       fileId1    INT(6) NOT NULL,
                       projectId2 INT(6) NOT NULL,
                       fileId2    INT(6) NOT NULL,
                       PRIMARY KEY(fileId1, fileId2),
                       INDEX (projectId1),
                       INDEX (fileId1),
                       INDEX (projectId2),
                       INDEX (fileId2)
                       ) ENGINE = MYISAM;"""
        cursor.execute(table)
    
        cursor.execute("DROP TABLE IF EXISTS `projectClones`;")
        table = """CREATE TABLE `projectClones` (
                       id                  INT(6)       UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
                       cloneId             INT(6)       UNSIGNED NOT NULL,
                       cloneClonedFiles    INT(6)       UNSIGNED NOT NULL,
                       cloneTotalFiles     INT(6)       UNSIGNED NOT NULL,
                       cloneCloningPercent DECIMAL(6,3) UNSIGNED NOT NULL,
                       hostId              INT(6)       UNSIGNED NOT NULL,
                       hostAffectedFiles   INT(6)       UNSIGNED NOT NULL,
                       hostTotalFiles      INT(6)       UNSIGNED NOT NULL,
                       hostAffectedPercent DECIMAL(6,3) UNSIGNED NOT NULL,
                       INDEX(cloneId),
                       INDEX(cloneClonedFiles),
                       INDEX(cloneTotalFiles),
                       INDEX(cloneCloningPercent),
                       INDEX(hostId),
                       INDEX(hostAffectedFiles),
                       INDEX(hostTotalFiles),
                       INDEX(hostAffectedPercent)
                       ) ENGINE = MYISAM;"""
        cursor.execute(table)


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

#    read_config(logging)
#    db_connect(logging)
    #tokenized_output = tokenize(logging)
    #run_SourcererCC(tokenized_output)
