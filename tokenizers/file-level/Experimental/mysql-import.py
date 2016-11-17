#Usage $python this_script.py folder-result-of-tokenizer/ list-of-pairs-from-CC

import sys, os, csv
import MySQLdb

def create_tables(DB_name,DB_user,DB_pass):

    try:
        db = MySQLdb.connect(host="localhost",  # your host, usually localhost
                             user=  DB_user,   # your username
                             passwd=DB_pass,     # your password
                             db=    DB_name) # name of the data base

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
                       fileId       BIGINT(6)     UNSIGNED PRIMARY KEY,
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
        print 'Error accessing Database'
        print e
        sys.exit(1)

    finally:
        cursor.close()
        db.commit()
        db.close()

def import_tokenizer_output(DB_name,DB_user,DB_pass,output_path):
    bookkeeping_file_path = os.path.join(output_path,'bookkeeping_projs')
    files_stats_path      = os.path.join(output_path,'files_stats')
    files_tokens_path     = os.path.join(output_path,'files_tokens')

    try:
        db = MySQLdb.connect(host="localhost", # your host, usually localhost
                             user=  DB_user,   # your username
                             passwd=DB_pass,   # your password
                             db=    DB_name)   # name of the data base

        cursor = db.cursor()

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
                        cursor.execute("""INSERT INTO projects VALUES (%s, %s, %s);""",
                                      (entry[0],entry[1][:4000],entry[2][:4000]))
                    db.commit()

        print '## Importing files and stats'
        # Insert values into projects Database
        for file in os.listdir(files_stats_path):
            if file.endswith('.stats'):
                file = os.path.join(files_stats_path,file)
                print '  Importing from ',file
                with open(file, 'r') as csvfile:
                    csv_reader = csv.reader(csvfile, delimiter=',')
                    for entry in csv_reader:

                        cursor.execute("""INSERT INTO files VALUES (%s, %s, %s, %s, %s);""",
                                       (entry[1],entry[0],entry[2],entry[3],entry[4]))

                        file_hash = entry[4]
                        cursor.execute("SELECT COUNT(*) FROM stats WHERE fileHash = '"+file_hash+"';")
                        (exists, ) = cursor.fetchone()

                        if exists == 0:
                            cursor.execute("""INSERT INTO stats VALUES (%s, %s, %s, %s, %s, %s, %s, %s);""",
                                           (file_hash,entry[5],entry[6],entry[7],entry[8],token_info[file_hash][0],token_info[file_hash][1],token_info[file_hash][2]))

                        db.commit()

    except Exception as e:
        print 'Error accessing Database'
        print e
        sys.exit(1)

    finally:
        cursor.close()
        db.commit()
        db.close()


if __name__ == "__main__":
    
    DB_name = sys.argv[1]
    output_path = sys.argv[2]
    
    DB_user = 'pribeiro'
    DB_pass = 'pass'
    #pairs_path = sys.argv[2]

    print '### Creating Tables'
    create_tables(DB_name,DB_user,DB_pass)
    print '### Importing output from tokenizer'
    import_tokenizer_output(DB_name,DB_user,DB_pass,output_path)

