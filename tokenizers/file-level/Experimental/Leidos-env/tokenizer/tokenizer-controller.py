import sys
import os
import logging
import datetime
import MySQLdb
import time
import configparser
from tokenizer import Tokenizer

PATH_logs = 'logs'
PATH_config_file = 'config.ini'
PATH_CC_input = ''
DB_user = ''
DB_pass = ''
DB_name = ''

def read_config(logging):
    global DB_user, DB_pass, DB_name

    # instantiate
    config = configparser.ConfigParser()

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
                       projectPath VARCHAR(4000)          NULL,
                       projectUrl  VARCHAR(4000)          NOT NULL
                       ) ENGINE = MYISAM; """
        cursor.execute(table)
        db.commit()
    
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

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print ('No arguments given. Usage: $pythoon this_script.py list-of-projects-folders.txt')
        sys.exit(1)

    target_folders = str(time.time())

    # Logging code
    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)
    file_handler = logging.FileHandler( os.path.join(PATH_logs,target_folders+'.log') )
    file_handler.setFormatter(logging.Formatter(FORMAT))
    logging.getLogger().addHandler(file_handler)

    logging.info('Starting tokenizer. Producibles (logs, output, etc) can be found under the name '+target_folders)

    read_config(logging)
    db_connect(logging)

    tokenizer = Tokenizer()
    #tokenized_output = tokenize(logging)
    #run_SourcererCC(tokenized_output)
