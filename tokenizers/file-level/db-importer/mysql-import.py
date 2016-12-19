#Usage $python this_script.py folder-result-of-tokenizer/ list-of-pairs-from-CC

import sys, os, csv
import MySQLdb
from db import DB
import logging

def create_tables(db, logging):

    logging.info('Creating Tables on DB')

    try:
    
        db.execute("DROP TABLE IF EXISTS `projects`;")
        table = """ CREATE TABLE IF NOT EXISTS `projects` (
                       projectId   INT(6)        UNSIGNED PRIMARY KEY,
                       projectPath VARCHAR(4000)          NOT NULL,
                       projectUrl  VARCHAR(4000)          NULL
                       ) ENGINE = InnoDB; """
        db.execute(table)
    
        db.execute("DROP TABLE IF EXISTS `files`;")
        table = """CREATE TABLE IF NOT EXISTS `files` (
                       fileId       BIGINT(6)     UNSIGNED PRIMARY KEY,
                       projectId    INT(6)        UNSIGNED NOT NULL,
                       relativePath VARCHAR(4000)          NULL,
                       relativeUrl  VARCHAR(4000)          NOT NULL,
                       fileHash     CHAR(32)               NOT NULL,
                       INDEX (projectId),
                       INDEX (fileHash)
                       ) ENGINE = InnoDB;"""
        db.execute(table)
    
        db.execute("DROP TABLE IF EXISTS `stats`;")
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
                       ) ENGINE = InnoDB;"""
        db.execute(table)
    
        db.execute("DROP TABLE IF EXISTS `CCPairs`;")
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
                       ) ENGINE = InnoDB;"""
        db.execute(table)
    
        db.execute("DROP TABLE IF EXISTS `projectClones`;")
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
                       ) ENGINE = InnoDB;"""
        db.execute(table)

    except Exception as e:
        logging.error('Error accessing Database')
        logging.error(e)
        sys.exit(1)

def import_tokenizer_output(db, output_path, logging):
    bookkeeping_file_path = os.path.join(output_path,'bookkeeping_projs')
    files_stats_path      = os.path.join(output_path,'files_stats')
    files_tokens_path     = os.path.join(output_path,'files_tokens')

    try:
        logging.info('## Warming up token values')
        #token_info = {}
        #for file in os.listdir(files_tokens_path):
        #    if file.endswith('.tokens'):
        #        file = os.path.join(files_tokens_path,file)
        #        logging.info('Getting info from '+file)
        #        with open(file, 'r') as csvfile:
        #            for line in csvfile:
        #                left = line.split('@#@')[0].split(',')
        #                token_info[left[1]] = [left[2],left[3],left[4]]

        #logging.info('## Import into database')

        logging.info('## Importing projects')
        # Insert values into projects Database
        for file in os.listdir(bookkeeping_file_path):
            if file.endswith('.projs'):
                file = os.path.join(bookkeeping_file_path,file)
                logging.info('Importing from '+file)
                with open(file, 'r') as csvfile:
                    for line in csvfile:
                        entry_split = (line[:-1]).split(',')

                        print entry_split

                        proj_id = entry_split[0]
                        del entry_split[0]

                        if (len(entry_split) % 2 != 0):
                            logging.warning('Problems parsing project: '+str(entry_split))
                            path = ','.join(entry_split[:len(entry_split)/2])
                            path = path[1:-1]
                            url  = ','.join(entry_split[len(entry_split)/2:])
                            url = url[1:-1]
                            logging.warning('String partitioned into:'+proj_id+'|'+path+'|'+url)
                            logging.warning('Previous warning was solved')
                            db.insert_project(proj_id,path,url)
                        else:
                            db.insert_project(proj_id,entry_split[0],entry_split[1])

        logging.info('## Importing files and stats')
        # Insert values into projects Database
        for file in os.listdir(files_stats_path):
            if file.endswith('.stats'):
                file = os.path.join(files_stats_path,file)
                logging.info('Importing from '+file)
                with open(file, 'r') as csvfile:
                    for entry in csvfile:
                        entry_split = (entry[:-1]).split(',')
                        if len(entry_split) != 9: # Something went wrong with parsing
                            logging.warning('Problematic file string: '+str(entry_split))

                            proj_id = entry_split[0]
                            del entry_split[0]
                            file_id = entry_split[0]
                            del entry_split[0]

                            sloc    = entry_split[-1:][0]
                            del entry_split[-1:]
                            loc   = entry_split[-1:][0]
                            del entry_split[-1:]
                            lines  = entry_split[-1:][0]
                            del entry_split[-1:]
                            bytess = entry_split[-1:][0]
                            del entry_split[-1:]
                            file_hash = entry_split[-1:][0]
                            file_hash = file_hash[1:-1]
                            del entry_split[-1:]

                            if (len(entry_split) % 2 != 0):
                                logging.error('Problems parsing file: '+str(entry_split))
                                continue
                            else:
                                path = ','.join(entry_split[:len(entry_split)/2])
                                path = path[1:-1]
                                url  = ','.join(entry_split[len(entry_split)/2:])
                                url = url[1:-1]

                            logging.warning('String partitioned into:'+file_id+'|'+proj_id+path+'|'+url+'|'+file_hash+'|'+bytess+'|'+lines+'|'+loc+'|'+sloc)

                            logging.warning('Previous warning was solved')
                            db.insert_file(fileId,proj_id,path,url,file_hash)
                            db.insert_stats_ignore_repetition( file_hash, bytess, lines, loc, sloc, token_info[fileId][0], token_info[fileId][1], token_info[fileId][2] )
                        else:
                            continue
                            db.insert_file(entry_split[1],entry_split[0],entry_split[2],entry_split[3],entry_split[4])
                            db.insert_stats_ignore_repetition( entry_split[4], entry_split[5], entry_split[6], entry_split[7], entry_split[8], token_info[entry_split[1]][0], token_info[entry_split[1]][1], token_info[entry_split[1]][2] )

    except Exception as e:
        logging.error('Error accessing Database')
        logging.error(e)
        sys.exit(1)

def import_pairs(db, pairs_path):
    try:

        commit_interval = 1000
        pair_number = 0

        print '## Importing pairs from',pairs_path
        with open(pairs_path, 'r') as file:
            for line in file:
                pair_number += 1
                line_split = line[:-1].split(',')

                db.insert_CCPairs(line_split[0], line_split[1], line_split[2], line_split[3])

                if pair_number%commit_interval == 0:
                    print '    ',pair_number,'pairs committed'

    except Exception as e:
        print 'Error accessing Database'
        print e
        sys.exit(1)

if __name__ == "__main__":
    user  = 'sourcerer'
    passw = 'sourcerer4us'

    log_path = 'LOG-db-importer.log'

    if os.path.isfile(log_path):
        print 'ERROR: Log file:',log_path,'already exists'
        sys.exit(1)

    FORMAT = '[%(levelname)s] (%(threadName)s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)
    file_handler = logging.FileHandler(log_path)
    file_handler.setFormatter(logging.Formatter(FORMAT))
    logging.getLogger().addHandler(file_handler)


    if len(sys.argv) == 1:
        logging.error('ERROR. At least 1 argument is required')
        sys.exit(1)
    if len(sys.argv) >= 2:
        DB_name     = sys.argv[1]
    if len(sys.argv) >= 3:
        output_path = sys.argv[2]
    if len(sys.argv) >= 4:
        pairs_path  = sys.argv[3]
    
    try:
        db_object = DB(user, DB_name, passw, logging)

        if len(sys.argv) >= 2:
            logging.info('### Creating Tables')
            create_tables(db_object,logging)
        if len(sys.argv) >= 3:
            logging.info('### Importing output from tokenizer')
            import_tokenizer_output(db_object,output_path,logging)
        if len(sys.argv) >= 4:
            logging.info('### Importing output from tokenizer')
            #import_pairs(pairs_path)
        
        db_object.close()

    except Exception as e:
        print 'Error on __main__'
        print e
    




