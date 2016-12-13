import sys, os
import mysql.connector
from mysql.connector import errorcode
import logging

DB_MAX_STRING_SIZE = 4000

table1 = """ CREATE TABLE IF NOT EXISTS `projects` (
               projectId   INT(6)        UNSIGNED PRIMARY KEY AUTO_INCREMENT,
               projectPath VARCHAR(%s)            NOT NULL,
               projectUrl  VARCHAR(%s)            NOT NULL
               ) ENGINE = MYISAM; """ % (DB_MAX_STRING_SIZE,DB_MAX_STRING_SIZE)

table2 = """CREATE TABLE IF NOT EXISTS `files` (
               fileId       BIGINT(6)     UNSIGNED PRIMARY KEY AUTO_INCREMENT,
               projectId    INT(6)        UNSIGNED NOT NULL,
               relativePath VARCHAR(%s)            NOT NULL,
               relativeUrl  VARCHAR(%s)            NOT NULL,
               fileHash     CHAR(32)               NOT NULL,
               INDEX (projectId),
               INDEX (fileHash)
               ) ENGINE = MYISAM;""" % (DB_MAX_STRING_SIZE,DB_MAX_STRING_SIZE)

table3 = """CREATE TABLE IF NOT EXISTS `stats` (
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

table4 = """CREATE TABLE IF NOT EXISTS `CCPairs` (
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

table5 = """CREATE TABLE IF NOT EXISTS `projectClones` (
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

add_projectClones = """INSERT INTO projectClones (cloneId,cloneClonedFiles,cloneTotalFiles,cloneCloningPercent,hostId,hostAffectedFiles,hostTotalFiles,hostAffectedPercent) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);"""
add_projects      = """INSERT INTO projects (projectId,projectPath,projectUrl) VALUES (NULL, %s, %s);"""
add_files         = """INSERT INTO files (fileId,projectId,relativePath,relativeUrl,fileHash) VALUES (NULL, %s, %s, %s, %s);"""
add_stats_and_check_tokenHash_uniqueness = """INSERT INTO stats (fileHash,fileBytes,fileLines,fileLOC,fileSLOC,totalTokens,uniqueTokens,tokenHash) VALUES (%s, %s, %s, %s, %s, %s, %s, %s); SELECT tokenHash FROM stats WHERE tokenHash = %s;"""
add_CCPairs       = """INSERT INTO CCPairs (projectId1,fileId1,projectId2,fileId2) VALUES (%s, %s, %s, %s);"""

check_fileHash    = """SELECT fileHash FROM stats WHERE fileHash = '%s';"""
project_exists    = """SELECT projectPath FROM projects WHERE projectPath = '%s';"""

class DB:
    # connection is a MySQLConnection object
    connection = None

    def __init__(self, DB_user, DB_name, DB_pass, logging):
        self.DB_user = DB_user
        self.DB_name = DB_name
        self.DB_pass = DB_pass
        self.logging = logging

        try:
            ## All cursors will be buffered by default
            self.connection = mysql.connector.connect(user=self.DB_user,password=self.DB_pass,host='localhost',buffered=True)
            
            #Causes a commit operation after each SQL statement.
            #Carefull setting autocommit to True, but it's appropriate for MyISAM, where transactions are not applicable.
            self.autocommit = True

            self.connection.database = DB_name
        except mysql.connector.Error as err:
            if err.errno == errorcode.ER_BAD_DB_ERROR:
                logging.warning('Database %s does not exist. Creating it now' % DB_name)
                self.create_database()
            else:
                logging.error('Cannot access DB %s with %s:%s' % (DB_name,DB_user,DB_pass))
                logging.error(err)
                exit(1)

    def create_database(self):
        cursor = self.connection.cursor()
        try:
            cursor.execute('CREATE DATABASE %s' % format(self.DB_name))
            self.connection.database = self.DB_name
            cursor.execute(table1)
            cursor.execute(table2)
            cursor.execute(table3)
            cursor.execute(table4)
            cursor.execute(table5)
        except Exception as err:
            self.logging.error('Failed to create DB %s with %s:%s' % (self.DB_name,self.DB_user,self.DB_pass))
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    def close(self):
        try:
            self.connection.close()
        except Exception as err:
            self.logging.error('Error on DB.close()')
            self.logging.error(e)

    def check_connection(self):
        try:
            if not self.connection.is_connected():
                self.connection.reconnect(attempts=10, delay=0)
        except Exception as err:
            self.logging.error('Unable to reconnect to %s' % (self.DB_name))
            self.logging.error(err)
            sys.exit(1)

    def insert_projectClones(self, cloneId, cloneClonedFiles, cloneTotalFiles, cloneCloningPercent, hostId, hostAffectedFiles, hostTotalFiles, hostAffectedPercent):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            cursor.execute(add_projectClones, (cloneId, cloneClonedFiles, cloneTotalFiles, cloneCloningPercent, hostId, hostAffectedFiles, hostTotalFiles, hostAffectedPercent))
            return cursor.lastrowid
        except Exception as err:
            self.logging.error('Failed to insert projectClone (clone %s, host %s)' % (cloneId, hostId))
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    def insert_project(self, projectPath, projectUrl):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            if projectUrl is None:
                cursor.execute(add_projects, (self.sanitize_string(projectPath), 'NULL'))
            else:
                cursor.execute(add_projects, (self.sanitize_string(projectPath), self.sanitize_string(projectUrl)) )
            return cursor.lastrowid
        except Exception as err:
            self.logging.error('Failed to insert project %s' % (projectPath))
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    def insert_file(self, proj_id, file_path, file_url, file_hash):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            if file_url is None:
                cursor.execute(add_files, (proj_id, self.sanitize_string(file_path), 'NULL', file_hash))
            else:
                cursor.execute(add_files, (proj_id, self.sanitize_string(file_path), self.sanitize_string(file_url), file_hash))
            return cursor.lastrowid
        except Exception as err:
            self.logging.error('Failed to insert file %s' % (file_path))
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    def get_max_project_id(self):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            cursor.execute("""SELECT Max(projectId) FROM projects;""")
            (id,) = cursor.fetchone()
            return id
        except Exception as err:
            self.logging.error('Failed to get max project id')
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    def insert_CCPairs(self, projectId1, fileId1, projectId2, fileId2):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            cursor.execute(add_CCPairs, (projectId1,fileId1,projectId2,fileId2))
            return cursor.lastrowid
        except Exception as err:
            self.logging.error('Failed to insert CCPairs %s,%s,%s,%s' % (projectId1,fileId1,projectId2,fileId2))
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    def fileHash_exists(self, file_hash):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            cursor.execute(check_fileHash % file_hash,params=False)
            if cursor.rowcount > 0:
                return True
            else:
                return False
        except Exception as err:
            self.logging.error('Cannot search for the file hash %s' % (file_hash))
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    # Add a note here
    def insert_stats_and_is_tokenHash_unique(self, fileHash, fileBytes, fileLines, fileLOC, fileSLOC, totalTokens, uniqueTokens, tokenHash):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            try:
                results = cursor.execute(add_stats_and_check_tokenHash_uniqueness, (fileHash, fileBytes, fileLines, fileLOC, fileSLOC, totalTokens, uniqueTokens, tokenHash, tokenHash), multi=True)
                #Execute with multi=True returns a generator, therefore:
                for cur in results:
                    if cur.with_rows:
                        if cur.rowcount > 0:
                            return True
                        else:
                            return False
            except mysql.connector.Error as err:
                if err.errno == errorcode.ER_DUP_ENTRY:
                    # If the error is because the entry is a duplicate we wont't care about it
                    return False
                else:
                    raise err
        except Exception as err:
            self.logging.error('Failed to insert stats for fileHash %s' % (fileHash))
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    def project_exists(self, proj_path):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            cursor.execute(project_exists % proj_path,params=False)
            if cursor.rowcount > 0:
                return True
            else:
                return False
        except Exception as err:
            self.logging.error('Cannot search for the project %s' % (self.sanitize_string(proj_path)) )
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

    def sanitize_string(self, string_input):
        # To clean non-ascii characters
        printable = set(string.printable)
        string_res = filter(lambda x: x in printable, string_input)
        return (string_res[:DB_MAX_STRING_SIZE])

    def execute(self, query):
        self.check_connection()
        cursor = self.connection.cursor()
        try:
            cursor.execute(query)
            if cursor.rowcount > 0:
                return cursor.fetchall()
            else:
                return []
        except Exception as err:
            self.logging.error('Failed to run query %s' % (query))
            self.logging.error(err)
            sys.exit(1)
        finally:
            cursor.close()

if __name__=='__main__':

    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)

    logging.info('__main__')
    db = DB('pribeiro','CPP','pass',logging)
    #print db.insert_project('la\"\"\"la\"\"\"t,his\'/is/a/pa,th)',None)
    #print db.fileHash_exists('0ee32c85c7df3fe7aa3c858478b0555c')
    #print db.insert_stats_and_is_tokenHash_unique('ssssddsdsddssdd',306,8,4,1,5,5,'37011874e03e4fs77ad44625095103d9')
    #print db.project_exists('\'/data/corpus_8tof/e/b/a/8/7/c/d/c/eba87cdc-d4f8-45f0-bdae-4e7d6253bd5f\'')
    #print db.insert_CCPairs(434,433,34,43)
    print db.get_max_project_id()
    db.close()



# 0ee32c85c7df3fe7aa3c858478b0555c | 306 | 8 | 4 | 1 | 5 | 5 | 317011874e03e4f77ad44625095103d9






