import sys
import os
import logging
import datetime
import MySQLdb
import time
import ConfigParser
from tokenizer import Tokenizer

PATH_logs = 'logs'
PATH_output = 'output'
PATH_config_file = 'config.ini'
PATH_CC_input = ''
DB_user = ''
DB_pass = ''
DB_name = ''

def read_config(logging):
    global DB_user, DB_pass, DB_name

    # instantiate
    config = ConfigParser.ConfigParser()

    # parse existing file
    try:
        config.read(PATH_config_file)
    except Exception as e:
        logging.error('ERROR on read_config')
        logging.error(e)
        sys.exit(1)

    # Get info from config.ini into global variables
    DB_user = config.get('MySQL', 'User')
    DB_pass = config.get('MySQL', 'Pass')
    DB_name = config.get('MySQL', 'Name')

    logging.info('Config file successfully read: %s:%s@%s' % (DB_user,DB_pass,DB_name))

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

        table = """ CREATE TABLE IF NOT EXISTS `projects` (
                       projectId   INT(6)        UNSIGNED PRIMARY KEY,
                       projectPath VARCHAR(4000)          NOT NULL,
                       projectUrl  VARCHAR(4000)          NULL
                       ) ENGINE = MYISAM; """
        cursor.execute(table)
        db.commit()
    
        table = """CREATE TABLE IF NOT EXISTS `files` (
                       fileId       BIGINT(6)     UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                       projectId    INT(6)        UNSIGNED NOT NULL,
                       relativePath VARCHAR(4000)          NOT NULL,
                       relativeUrl  VARCHAR(4000)          NULL,
                       fileHash     CHAR(32)               NOT NULL,
                       INDEX (projectId),
                       INDEX (fileHash)
                       ) ENGINE = MYISAM;"""
        cursor.execute(table)
        db.commit()
    
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
    
        table = """CREATE TABLE IF NOT EXISTS `CCPairs` (
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
    
        table = """CREATE TABLE IF NOT EXISTS `projectClones` (
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
        logging.error('Error on db_connect')
        logging.error(e)
        sys.exit(1)

    finally:
        cursor.close()
        db.close()

    logging.info('Database \''+DB_name+'\' successfully initialized')

def sanitize_strings(string):
    return ('\"'+ (string.replace('\"','\'')[:4000]) +'\"')

def read_file_paths(list_of_projects, logging):
    if not os.path.isfile(sys.argv[1]):
        logging.error('File [%s] does not exist!' % list_of_projects )
        sys.exit(1)
    else:
        try:
            proj_paths = set()
            db = MySQLdb.connect(host="localhost", # your host, usually localhost
                                 db=DB_name,
                                 user=DB_user,     # your username
                                 passwd=DB_pass)   # your password
            cursor = db.cursor()

            with open(list_of_projects) as f:
                for line in f:
                    proj_path = line.replace('\n','')

                    q = "SELECT COUNT(*) FROM projects WHERE projectPath=%s" % (sanitize_strings(proj_path))
                    cursor.execute(q)

                    (exists,) = cursor.fetchone()
                    if exists == 0:
                        proj_paths.add( proj_path )

            logging.info('List of project paths successfully read. Ready to process %s new projects.' % len(proj_paths))
            return proj_paths

        except Exception as e:
            logging.error('Error on read_file_paths')
            logging.error(e)
            sys.exit(1)

        finally:
            cursor.close()
            db.commit()
            db.close()

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print ('No arguments given. Usage: $pythoon this_script.py list-of-projects-folders.txt')
        sys.exit(1)

    target_folders = str(time.time())

    # Creating folder for the processes logs
    logs_folder   = os.path.join(PATH_logs,target_folders)
    if os.path.exists( logs_folder ):
        logging.error('Folder [%s] already exists!' % logs_folder )
        sys.exit(1)
    else:
        os.makedirs(logs_folder)

    # Create folder for processes output
    output_folder = os.path.join(PATH_output,target_folders)
    if os.path.exists( output_folder ):
        logging.error('Folder [%s] already exists!' % output_folder )
        sys.exit(1)
    else:
        os.makedirs(output_folder)

    # Logging code
    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)
    file_handler = logging.FileHandler( os.path.join(logs_folder,'tokenizer.log') )
    file_handler.setFormatter(logging.Formatter(FORMAT))
    logging.getLogger().addHandler(file_handler)

    read_config(logging)
    db_connect(logging)
    proj_paths = read_file_paths(sys.argv[1], logging)
    if len(proj_paths) > 0:
        logging.info('Starting tokenizer. Producibles (logs, output, etc) can be found under the name '+target_folders)
        tokenizer = Tokenizer(proj_paths, DB_user, DB_pass, DB_name, logging, logs_folder, output_folder, 2, 2)
        tokenizer.execute()
    else:
        logging.info('The list of new projects is empty (or these are already on the DB).')
