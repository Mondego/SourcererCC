import sys
import os
import logging
import datetime
import MySQLdb
import time
import ConfigParser
from tokenizer import Tokenizer
from db import DB

class TokenizerController(object):

    PATH_logs        = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'logs')
    PATH_output      = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'output')
    PATH_config_file = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'config.ini')
    PATH_CC_input = ''
    DB_user = ''
    DB_pass = ''
    DB_name = ''
    N_PROCESSES = 2
    BATCH_SIZE = 10
    PROJECTS_CONFIGURATION = 'GithubZIP' # alternatives: 'GithubZIP', 'Leidos'
    PATH_CC = os.path.abspath("../clone-detector")

    def read_config(self):
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

        self.read_config()

        db = DB('pribeiro','CPP','pass',logging)
        logging.info('Database \''+self.DB_name+'\' successfully initialized')
        db.close()

        self.proj_paths = self.read_file_paths(file_list_projects)

    def execute(self):
        if len(self.proj_paths) > 0:
            logging.info('Starting tokenizer. Producibles (logs, output, etc) can be found under the name '+self.target_folders)
            tokenizer = Tokenizer(self.proj_paths, self.DB_user, self.DB_pass, self.DB_name, logging, self.logs_folder, self.output_folder, self.N_PROCESSES, self.BATCH_SIZE, self.PROJECTS_CONFIGURATION)
            tokenizer.execute()
        else:
            logging.warning('The list of new projects is empty (or these are already on the DB).')

    def read_file_paths(self, list_of_projects):
        if not os.path.isfile(list_of_projects):
            logging.error('File [%s] does not exist!' % list_of_projects )
            sys.exit(1)
        else:
            db = DB(self.DB_user, self.DB_name, self.DB_pass, logging)
            try:
                proj_paths = set()

                with open(list_of_projects) as f:
                    for line in f:
                        proj_path = line.replace('\n','')

                        if not db.project_exists(proj_path):
                            proj_paths.add( proj_path )

                if len(proj_paths) > 0:
                    logging.info('List of project paths successfully read. Ready to process %s new projects.' % len(proj_paths))
                else:
                    logging.warning('The list of new projects is empty (or these are already on the DB).')
                    sys.exit(1)

                db.close()
                return proj_paths

            except Exception as e:
                logging.error('Error on read_file_paths')
                logging.error(e)
                db.close()
                sys.exit(1)

    def move_input_to_CC(self):
        #print self.output_folder
        #print self.PATH_CC

        if not os.path.exists( self.output_folder ):
            logging.error('ERROR - Folder [%s] does not exist!' % self.output_folder )
            sys.exit(1)

        if not os.path.exists( os.path.join(self.PATH_CC,'input','dataset') ):
            logging.error('ERROR - Folder [%s] does not exist!' % self.PATH_CC )
            sys.exit(1)

        new_files_counter = 0
        cc_input_file = os.path.join(self.PATH_CC,'input','dataset','blocks.file')
        with open(cc_input_file,'w') as CCinput:
            for file in os.listdir(self.output_folder):
                if file.endswith('.tokens'):
                    file = os.path.join(self.output_folder,file)
                    with open(file,'r') as token_file:
                        for line in token_file:
                            CCinput.write(line)
                            new_files_counter += 1

        if new_files_counter == 0:
            logging.warning('No new input for SourcererCC. Stopping.')
            os.remove(cc_input_file)
            sys.exit(0)
        else:
            logging.info('%s token-hash distinct files to be processed by CC, in %s' % (new_files_counter,cc_input_file))

    def import_pairs_to_DB(self):
        try:
            db = DB(self.DB_user, self.DB_name, self.DB_pass, logging)

            log_interval = 1000
            pair_number = 0

            cc_backup_folder = os.path.join(self.PATH_CC,'backup_output')

            latest_folder = 0
            for folder in os.listdir(cc_backup_folder):
                if folder.isdigit():
                    if int(folder) > latest_folder:
                        latest_folder = int(folder)
            
            cc_backup_folder = os.path.join(cc_backup_folder,str(latest_folder))
            logging.info('Copying CC pairs from %s' % (cc_backup_folder))


            for folder in os.listdir(cc_backup_folder):
                if folder.startswith('NODE_'):
                    pairs_file = os.path.join(cc_backup_folder,folder,'queryclones_index_WITH_FILTER.txt')
                    if os.path.isfile(pairs_file):
                        logging.info('Reading %s' % pairs_file)
                        with open(pairs_file,'r') as result:
                            for pair in result:
                                pair_number += 1
                                line_split = pair[:-1].split(',')

                                db.insert_CCPairs(line_split[0],line_split[1],line_split[2],line_split[3])

                                if pair_number%log_interval == 0:
                                    logging.info('%s pairs imported to the DB' % (pair_number))
                    else:
                        logging.error('File %s not found' % (pairs_file))

            db.close()

        except Exception as e:
            logging.error('Error on TokenizerController.import_pairs_to_DB')
            logging.error(e)

if __name__ == '__main__':
    print 'tokenizerController.__main__'
    tokenizerController = TokenizerController(sys.argv[1])
