import sys, os
import mysql.connector
from mysql.connector import errorcode
import logging

DB_MAX_STRING_SIZE = 4000

table1 = """ CREATE TABLE IF NOT EXISTS `projects` (
               projectId   INT(6)        UNSIGNED PRIMARY KEY AUTO_INCREMENT,
               projectPath VARCHAR(%s)            NOT NULL,
               projectUrl  VARCHAR(%s)            NULL
               ) ENGINE = MYISAM; """ % (DB_MAX_STRING_SIZE,DB_MAX_STRING_SIZE)

table2 = """CREATE TABLE IF NOT EXISTS `files` (
               fileId       BIGINT(6)     UNSIGNED PRIMARY KEY AUTO_INCREMENT,
               projectId    INT(6)        UNSIGNED NOT NULL,
               relativePath VARCHAR(%s)            NOT NULL,
               relativeUrl  VARCHAR(%s)            NULL,
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

class DB:
    # connection is a MySQLConnection object
    connection = None

    def __init__(self, DB_user, DB_name, DB_pass, logging):
        self.DB_user = DB_user
        self.DB_name = DB_name
        self.DB_pass = DB_pass
        self.logging = logging

        ## All cursors will be buffered by default
        self.connection = mysql.connector.connect(user=self.DB_user,password=self.DB_pass,host='localhost',buffered=True)
        
        #Causes a commit operation after each SQL statement.
        #Carefull setting autocommit to True, but it's appropriate for MyISAM, where transactions are not applicable.
        self.autocommit = True

        try:
            self.connection.database = DB_name
        except mysql.connector.Error as err:
            if err.errno == errorcode.ER_BAD_DB_ERROR:
                logging.info.warning('Database %s does not exist. Creating it now' % DB_name)
                self.create_database()
            else:
                logging.error('Cannot access DB %s with %s:%s' % (DB_name,DB_user,DB_pass))
                logging.error(err)
                exit(1)

    def create_database(self):
        cursor = self.connection.cursor(buffered=True)
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
        cursor = self.connection.cursor(buffered=True)
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
        cursor = self.connection.cursor(buffered=True)
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

    def sanitize_string(self, string):
        return (string[:DB_MAX_STRING_SIZE])

    def execute(self, query):
        self.check_connection()
        cursor = self.connection.cursor(buffered=True)
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
    db = DB('pribeiro','LA','pass',logging)
    print db.insert_project('la\"\"\"lathis\'/is/a/path',None)
    db.close()
