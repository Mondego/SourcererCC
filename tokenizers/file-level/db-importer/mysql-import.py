#Usage $python this_script.py folder-result-of-tokenizer/ list-of-pairs-from-CC

import sys, os, csv
import MySQLdb
from db import DB
import logging

def create_tables(db):

    try:
    
        db.execute("DROP TABLE IF EXISTS `projects`;")
        table = """ CREATE TABLE IF NOT EXISTS `projects` (
                       projectId   INT(6)        UNSIGNED PRIMARY KEY,
                       projectPath VARCHAR(4000)          NOT NULL,
                       projectUrl  VARCHAR(4000)          NULL
                       ) ENGINE = MYISAM; """
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
                       ) ENGINE = MYISAM;"""
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
                       ) ENGINE = MYISAM;"""
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
                       ) ENGINE = MYISAM;"""
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
                       ) ENGINE = MYISAM;"""
        db.execute(table)

    except Exception as e:
        print 'Error accessing Database'
        print e
        sys.exit(1)

def import_tokenizer_output(db, output_path):
    bookkeeping_file_path = os.path.join(output_path,'bookkeeping_projs')
    files_stats_path      = os.path.join(output_path,'files_stats')
    files_tokens_path     = os.path.join(output_path,'files_tokens')

    try:
        print '## Warming up token values'
        token_info = {}
        for file in os.listdir(files_tokens_path):
            if file.endswith('.tokens'):
                file = os.path.join(files_tokens_path,file)
                print '  Getting info from ',file
                with open(file, 'r') as csvfile:
                    for line in csvfile:
                        left = line.split('@#@')[0].split(',')
                        token_info[left[1]] = [left[2],left[3],left[4]]
    
        print '## Import into database'

        print '## Importing projects'
        # Insert values into projects Database
        for file in os.listdir(bookkeeping_file_path):
            if file.endswith('.projs'):
                file = os.path.join(bookkeeping_file_path,file)
                print '  Importing from ',file
                with open(file, 'r') as csvfile:
                    csv_reader = csv.reader(csvfile, delimiter=',')
                    for entry in csv_reader:
                        db.insert_project(entry[0],entry[1],entry[2])

        print '## Importing files and stats'
        # Insert values into projects Database
        for file in os.listdir(files_stats_path):
            if file.endswith('.stats'):
                file = os.path.join(files_stats_path,file)
                print '  Importing from ',file
                with open(file, 'r') as csvfile:
                    csv_reader = csv.reader(csvfile, delimiter=',')
                    for entry in csv_reader:

                        db.insert_file(entry[1],entry[0],entry[2],entry[3],entry[4])

                        file_hash = entry[4]
                        if not db.fileHash_exists(file_hash):
                            db.insert_stats_and_is_tokenHash_unique( file_hash, entry[5], entry[6], entry[7], entry[8], token_info[entry[1]][0], token_info[entry[1]][1], token_info[entry[1]][2] )

    except Exception as e:
        print 'Error accessing Database'
        print e
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
    user  = 'pribeiro'
    passw = 'pass'

    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)

    if len(sys.argv) == 1:
        print 'ERROR. At least 1 argument is required'
    if len(sys.argv) >= 2:
        DB_name     = sys.argv[1]
    if len(sys.argv) >= 3:
        output_path = sys.argv[2]
    if len(sys.argv) >= 4:
        pairs_path  = sys.argv[3]
    
    try:
        db_object = DB(user, DB_name, passw, logging)

        if len(sys.argv) >= 2:
            print '### Creating Tables'
            create_tables(db_object)
        if len(sys.argv) >= 3:
            print '### Importing output from tokenizer'
            import_tokenizer_output(db_object,output_path)
        if len(sys.argv) >= 4:
            print '### Importing output from tokenizer'
            import_pairs(pairs_path)
        
        db_object.close()

    except Exception as e:
        print 'Error on __main__'
        print e
    




