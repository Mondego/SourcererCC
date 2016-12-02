import sys
import os
import logging
import datetime
import MySQLdb
import time
import ConfigParser
from tokenizer import Tokenizer

class TokenizerController(object):

    PATH_logs = 'logs'
    PATH_output = 'output'
    PATH_config_file = 'config.ini'
    PATH_CC_input = ''
    DB_user = ''
    DB_pass = ''
    DB_name = ''
    N_PROCESSES = 2
    BATCH_SIZE = 10

    def read_config(self, logging):
        # instantiate
        config = ConfigParser.ConfigParser()

        # parse existing file
        try:
            config.read(self.PATH_config_file)
        except Exception as e:
            logging.error('ERROR on read_config')
            logging.error(e)
            sys.exit(1)

        # Get info from config.ini into global variables
        self.DB_user = config.get('MySQL', 'User')
        self.DB_pass = config.get('MySQL', 'Pass')
        self.DB_name = config.get('MySQL', 'Name')

        logging.info('Config file successfully read: %s:%s@%s' % (self.DB_user,self.DB_pass,self.DB_name))

    def __init__(self, file_list_projects):
        self.target_folders = str(time.time())

        # Creating folder for the processes logs
        self.logs_folder   = os.path.join(self.PATH_logs,self.target_folders)
        if os.path.exists( self.logs_folder ):
            logging.error('Folder [%s] already exists!' % self.logs_folder )
            sys.exit(1)
        else:
            os.makedirs(self.logs_folder)

        # Create folder for processes output
        self.output_folder = os.path.join(self.PATH_output,self.target_folders)
        if os.path.exists( self.output_folder ):
            logging.error('Folder [%s] already exists!' % self.output_folder )
            sys.exit(1)
        else:
            os.makedirs(self.output_folder)

        # Logging code
        FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
        logging.basicConfig(level=logging.DEBUG,format=FORMAT)
        file_handler = logging.FileHandler( os.path.join(self.logs_folder,'tokenizer.log') )
        file_handler.setFormatter(logging.Formatter(FORMAT))
        logging.getLogger().addHandler(file_handler)

        self.read_config(logging)
        self.db_connect(logging)
        self.proj_paths = self.read_file_paths(file_list_projects, logging)

        if len(self.proj_paths) > 0:
            logging.info('Starting tokenizer. Producibles (logs, output, etc) can be found under the name '+self.target_folders)
            tokenizer = Tokenizer(self.proj_paths, self.DB_user, self.DB_pass, self.DB_name, logging, self.logs_folder, self.output_folder, self.N_PROCESSES, self.BATCH_SIZE)
            tokenizer.execute()
        else:
            logging.info('The list of new projects is empty (or these are already on the DB).')


    def read_file_paths(self, list_of_projects, logging):
        if not os.path.isfile(list_of_projects):
            logging.error('File [%s] does not exist!' % list_of_projects )
            sys.exit(1)
        else:
            try:
                proj_paths = set()
                db = MySQLdb.connect(host="localhost", # your host, usually localhost
                                     db=self.DB_name,
                                     user=self.DB_user,     # your username
                                     passwd=self.DB_pass)   # your password
                cursor = db.cursor()

                with open(list_of_projects) as f:
                    for line in f:
                        proj_path = line.replace('\n','')

                        q = "SELECT COUNT(*) FROM projects WHERE projectPath=%s" % (self.sanitize_strings(proj_path))
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

    def db_connect(self, logging):
        try:
            db = MySQLdb.connect(host="localhost", # your host, usually localhost
                                 user=self.DB_user,     # your username
                                 passwd=self.DB_pass)   # your password
            cursor = db.cursor()
            cursor.execute('CREATE DATABASE IF NOT EXISTS %s;' % self.DB_name)
            db.commit()
            db.close()

            db = MySQLdb.connect(host="localhost", # your host, usually localhost
                                 db=self.DB_name,
                                 user=self.DB_user,     # your username
                                 passwd=self.DB_pass)   # your password
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

        logging.info('Database \''+self.DB_name+'\' successfully initialized')

    def sanitize_strings(self, string):
        return ('\"'+ (string.replace('\"','\'')[:4000]) +'\"')

if __name__ == '__main__':
    print '__main__'
    tokenizerController = TokenizerController(sys.argv[1])
