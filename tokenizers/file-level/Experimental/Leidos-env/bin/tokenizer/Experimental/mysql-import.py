#Usage $python this_script.py folder-result-of-tokenizer/ list-of-pairs-from-CC

import sys, os, csv
import MySQLdb

bookkeeping_file_path = sys.argv[1]+'bookkeeping_projs'
files_stats_path = sys.argv[1]+'files_stats'
files_tokens_path = sys.argv[1]+'files_tokens'
pairs_path = sys.argv[2]

try:
    db = MySQLdb.connect(host="localhost",  # your host, usually localhost
                         user="USER",   # your username
                         passwd="PASS",     # your password
                         db="DB") # name of the data base
except:
    print 'Error accessing Database'
    sys.exit(1)

cursor = db.cursor()


print '### Creating Table projects and importing values'
# CREATE projects Database
cursor.execute("DROP TABLE IF EXISTS `projects`;")
projectsTable = """ CREATE TABLE `projects` (
                           projectId INT(6) UNSIGNED PRIMARY KEY,
                           projectLeidosPath VARCHAR(1000) NOT NULL,
                           projectUrl VARCHAR(1000) NOT NULL
                           ) ENGINE = MYISAM; """

cursor.execute(projectsTable)
db.commit()

# Insert values into projects Database
for file in os.listdir(bookkeeping_file_path):
    if file.endswith('.projs'):
        file = os.path.join(bookkeeping_file_path,file)
        print '  Importing from ',file
        with open(file, 'r') as csvfile:
            csv_reader = csv.reader(csvfile, delimiter=',')
            for entry in csv_reader:
                cursor.execute("""INSERT INTO projects VALUES (%s, %s, %s);""",
                              (entry[0],entry[1],entry[2]))
            db.commit()


print '### Creating Table filesStats and importing values'
# CREATE filesStats Database
cursor.execute("DROP TABLE IF EXISTS `filesStats`;")
filesStatsTable = """ CREATE TABLE `filesStats` (
                           projectId INT(7) UNSIGNED NOT NULL,
                           fileId INT(9) UNSIGNED PRIMARY KEY,
                           fileLeidosPath VARCHAR(1000) NOT NULL,
                           fileUrl VARCHAR(1000),
                           fileHash VARCHAR(32) NOT NULL,
                           fileBytes INT(7) UNSIGNED NOT NULL,
                           fileLines INT(7) UNSIGNED NOT NULL,
                           fileLOC INT(7) UNSIGNED NOT NULL,
                           fileSLOC INT(7) UNSIGNED NOT NULL,
                           INDEX (projectId),
                           INDEX (fileHash)
                           ) ENGINE = MYISAM; """

cursor.execute(filesStatsTable)
db.commit()

# Insert values into projects Database
for file in os.listdir(files_stats_path):
    if file.endswith('.stats'):
        file = os.path.join(files_stats_path,file)
        print '  Importing from ',file
        with open(file, 'r') as csvfile:
            csv_reader = csv.reader(csvfile, delimiter=',')
            for entry in csv_reader:
                print entry
                cursor.execute("""INSERT INTO filesStats VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);""",
                              (entry[0],entry[1],entry[2],entry[3],entry[4],entry[5],entry[6],entry[7],entry[8]))
            db.commit()


print '### Creating Table filesTokens and importing values'
# CREATE filesTokens Database
cursor.execute("DROP TABLE IF EXISTS `filesTokens`;")
filesTokensTable = """ CREATE TABLE `filesTokens` (
                           projectId INT(7) UNSIGNED NOT NULL,
                           fileId INT(9) UNSIGNED PRIMARY KEY,
                           totalTokens INT(7) UNSIGNED NOT NULL,
                           uniqueTokens INT(7) UNSIGNED NOT NULL,
                           tokenHash VARCHAR(32) NOT NULL,
                           INDEX (projectId),
                           INDEX (tokenHash)
                           ) ENGINE = MYISAM; """

cursor.execute(filesTokensTable)
db.commit()

# Insert values into projects Database
for file in os.listdir(files_tokens_path):
    if file.endswith('.tokens'):
        file = os.path.join(files_tokens_path,file)
        print '  Importing from ',file
        with open(file, 'r') as csvfile:
            for line in csvfile:
                left = line.split('@#@')[0].split(',')
                cursor.execute("""INSERT INTO filesTokens VALUES (%s, %s, %s, %s, %s);""",
                              (left[0],left[1],left[2],left[3],left[4]))
            db.commit()


print '### Creating Table CCPairs and importing values'
# CREATE filesTokens Database
cursor.execute("DROP TABLE IF EXISTS `CCPairs`;")
ccPairs = """ CREATE TABLE `CCPairs` (
                id INT(9) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
                projectLeft INT(7) UNSIGNED NOT NULL,
                fileLeft INT(9) UNSIGNED NOT NULL,
                projectRight INT(7) UNSIGNED NOT NULL,
                fileRight INT(9) UNSIGNED NOT NULL,
                INDEX (projectLeft),
                INDEX (fileLeft),
                INDEX (projectRight),
                INDEX (fileRight)
                ) ENGINE = MYISAM; """

cursor.execute(ccPairs)
db.commit()

# Insert values into projects Database
pair_number = 0
commit_interval = 1000000
print '  Importing from ',pairs_path
with open(pairs_path, 'r') as pairsfile:
    for line in pairsfile:
        line_split = line[:-1].split(',')
        cursor.execute("""INSERT INTO CCPairs (projectLeft,fileLeft,projectRight,fileRight) VALUES (%s, %s, %s, %s);""",
                      (line_split[0],line_split[1],line_split[2],line_split[3]))
        pair_number += 1
        if pair_number%commit_interval == 0:
            db.commit()
            print '    ',pair_number,'CC pairs commited ...'
    db.commit()
    print '    all ',pair_number,'CC pairs imported'

db.close()

