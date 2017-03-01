import sys, os
import mysql.connector
from mysql.connector import errorcode
import logging
import string

DB_MAX_STRING_SIZE = 4000
# The values below depend on max_allowed_packet in the mysql config
# For buffer sizes of 100,000, make max_allowed_packet = 16G
FILES_BUFFER_SIZE = 100000
FILES_STATS_BUFFER_SIZE = 100000
PROJECT_CLONES_BUFFER_SIZE = 1000
BLOCKS_BUFFER_SIZE = 100000
BLOCKS_STATS_BUFFER_SIZE = 100000

table1 = """ CREATE TABLE IF NOT EXISTS `projects` (
        projectId   INT(8)        UNSIGNED PRIMARY KEY AUTO_INCREMENT,
        projectPath VARCHAR(%s)            NULL,
        projectUrl  VARCHAR(%s)            NULL
        ) Engine=MyISAM; """ % (DB_MAX_STRING_SIZE,DB_MAX_STRING_SIZE)

table2 = """CREATE TABLE IF NOT EXISTS `files` (
        fileId       BIGINT(6)     UNSIGNED PRIMARY KEY AUTO_INCREMENT,
        projectId    INT(8)        UNSIGNED NOT NULL,
        relativePath VARCHAR(%s)            NULL,
        relativeUrl  VARCHAR(%s)            NULL,
        fileHash     CHAR(32)               NOT NULL,
        INDEX (projectId),
        INDEX (fileHash)
        ) Engine=MyISAM;""" % (DB_MAX_STRING_SIZE,DB_MAX_STRING_SIZE)

table3 = """CREATE TABLE IF NOT EXISTS `stats` (
        fileHash     CHAR(32)        PRIMARY KEY,
        fileBytes    INT(6) UNSIGNED NOT NULL,
        fileLines    INT(6) UNSIGNED NOT NULL,
        fileLOC      INT(6) UNSIGNED NOT NULL,
        fileSLOC     INT(6) UNSIGNED NOT NULL,
        totalTokens  INT(6) UNSIGNED,
        uniqueTokens INT(6) UNSIGNED,
        tokenHash    CHAR(32),
        INDEX (tokenHash)
        ) Engine=MyISAM;"""

table4 = """CREATE TABLE IF NOT EXISTS `CCPairs` (
        projectId1 INT(8)     NOT NULL,
        fileId1    BIGINT(15) NOT NULL,
        projectId2 INT(8)     NOT NULL,
        fileId2    BIGINT(15) NOT NULL,
        PRIMARY KEY(fileId1, fileId2),
        INDEX (projectId1),
        INDEX (fileId1),
        INDEX (projectId2),
        INDEX (fileId2)
        ) Engine=MyISAM;"""

table5 = """CREATE TABLE IF NOT EXISTS `projectClones` (
        id                  INT(6)       UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
        cloneId             INT(8)       UNSIGNED NOT NULL,
        cloneClonedFiles    INT(6)       UNSIGNED NOT NULL,
        cloneTotalFiles     INT(6)       UNSIGNED NOT NULL,
        cloneCloningPercent DECIMAL(6,3) UNSIGNED NOT NULL,
        hostId              INT(8)       UNSIGNED NOT NULL,
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
        ) Engine=MyISAM;"""

table6 = """CREATE TABLE IF NOT EXISTS `blocks` (
        id             INT(6)          UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
        projectId      INT(8)          UNSIGNED NOT NULL,
        fileId         BIGINT(15)      UNSIGNED,
        blockId        BIGINT(15)      UNSIGNED NOT NULL,
        blockhash      CHAR(32)        NOT NULL,
        blockStartLine INT(6) UNSIGNED,
        blockEndLine   INT(6) UNSIGNED,
        INDEX (projectId),
        INDEX (fileId),
        INDEX (blockhash)
        ) Engine=MyISAM;"""

table7 = """CREATE TABLE IF NOT EXISTS `blockStats` (
        blockhash      CHAR(32)          PRIMARY KEY,
        blockLines     INT(6)   UNSIGNED NOT NULL,
        blockLOC       INT(6)   UNSIGNED NOT NULL,
        blockSLOC      INT(6)   UNSIGNED NOT NULL,
        totalTokens    INT(6)   UNSIGNED NOT NULL,
        uniqueTokens   INT(6)   UNSIGNED NOT NULL,
        tokenHash      CHAR(32)          NOT NULL,
        INDEX (tokenHash)
        ) Engine=MyISAM;"""

add_projectClones = """INSERT IGNORE INTO projectClones (cloneId,cloneClonedFiles,cloneTotalFiles,cloneCloningPercent,hostId,hostAffectedFiles,hostTotalFiles,hostAffectedPercent) VALUES %s;"""
clone_list        = "(%s, %s, %s, %s, %s, %s, %s, %s)"
add_projects      = """INSERT INTO projects (projectId,projectPath,projectUrl) VALUES (%s, %s, %s);"""
add_files         = """INSERT INTO files (fileId,projectId,relativePath,relativeUrl,fileHash) VALUES %s ;"""
files_list        = "('%s', '%s', '%s', '%s', '%s')"
add_files_stats_ignore_repetition = """INSERT IGNORE INTO stats (fileHash,fileBytes,fileLines,fileLOC,fileSLOC,totalTokens,uniqueTokens,tokenHash) VALUES %s ;"""
files_stats_list  = "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')"
add_stats_and_check_tokenHash_uniqueness = """INSERT INTO stats (fileHash,fileBytes,fileLines,fileLOC,fileSLOC,totalTokens,uniqueTokens,tokenHash) VALUES (%s, %s, %s, %s, %s, %s, %s, %s); SELECT tokenHash FROM stats WHERE tokenHash = %s;"""
add_CCPairs       = """INSERT INTO CCPairs (projectId1,fileId1,projectId2,fileId2) VALUES (%s, %s, %s, %s);"""
check_fileHash    = """SELECT fileHash FROM stats WHERE fileHash = '%s';"""
project_exists    = """SELECT projectPath FROM projects WHERE projectPath = '%s';"""
add_blocks_stats_ignore_repetition = """INSERT IGNORE INTO blockStats (blockHash, blockLines, blockLOC, blockSLOC, totalTokens, uniqueTokens, tokenHash) VALUES %s ;"""
blocks_list       = "('%s', '%s', '%s', '%s', '%s', '%s')"
add_blocks        = """INSERT IGNORE INTO blocks (projectId, fileId, blockId, blockhash, blockStartLine, blockEndLine) VALUES %s ;"""
blocks_stats_list = "('%s', '%s', '%s', '%s', '%s', '%s', '%s')"

add_projects_autoId = """INSERT INTO projects (projectPath,projectUrl) VALUES (%s, %s);"""
add_files_autoId    = """INSERT INTO files (projectId,relativePath,relativeUrl,fileHash) VALUES %s ;"""

class DB:
  # connection is a MySQLConnection object
  connection = None

  def __init__(self, DB_user, DB_name, DB_pass, logging):
    self.DB_user = DB_user
    self.DB_name = DB_name
    self.DB_pass = DB_pass
    self.logging = logging

    self.files = []
    self.files_stats = []
    self.file_count = 0
    self.clones = []
    self.blocks = []
    self.blocks_stats = []
    self.block_count = 0

    try:
      ## All cursors will be buffered by default
      self.connection = mysql.connector.connect(user=self.DB_user,password=self.DB_pass,host='localhost')
      
      #Causes a commit operation after each SQL statement.
      #Carefull setting autocommit to True, but it's appropriate for MyISAM, where transactions are not applicable.
      #self.autocommit = True

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
      cursor.execute(table6)
      cursor.execute(table7)
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

  def insert_projectClones(self, cloneId, cloneClonedFiles, cloneTotalFiles, cloneCloningPercent, 
               hostId, hostAffectedFiles, hostTotalFiles, hostAffectedPercent, flush = False):
    if not flush:
      self.clones.append( clone_list % (cloneId, cloneClonedFiles, cloneTotalFiles, cloneCloningPercent, 
                        hostId, hostAffectedFiles, hostTotalFiles, hostAffectedPercent) )
      if len(self.clones) < PROJECT_CLONES_BUFFER_SIZE:
        return

    clist = ','.join(self.clones)

    self.check_connection()
    cursor = self.connection.cursor()
    try:
      cursor.execute(add_projectClones % (clist))
      return cursor.lastrowid
    except Exception as err:
      self.logging.error('Failed to insert projectClone (clone %s, host %s)' % (cloneId, hostId))
      self.logging.error(err)
      sys.exit(1)
    finally:
      cursor.close()
      self.clones = []

  def flush_projectClones(self):
    if len(self.clones) > 0:
      self.insert_projectClones(None, None, None, None, None, None, None, None, flush = True)

  def insert_project(self, proj_id, projectPath, projectUrl, autoID = False):
    self.check_connection()
    cursor = self.connection.cursor()

    if projectUrl is None:
      projectUrl = 'NULL' 

    try:
      cursor.execute(add_projects_autoId, (self.sanitize_string(projectPath), self.sanitize_string(projectUrl)))
      return cursor.lastrowid
    except Exception as err:
      self.logging.error('Failed to insert project %s' % (projectPath))
      self.logging.error(err)
      sys.exit(1)
    finally:
      cursor.close()

  def insert_files_stats_ignore_repetition(self, fileHash, fileBytes, fileLines, fileLOC, fileSLOC, totalTokens, uniqueTokens, tokenHash, flush = False):
    if not flush:
      self.files_stats.append( files_stats_list % (fileHash, fileBytes, fileLines, fileLOC, fileSLOC, totalTokens, uniqueTokens, tokenHash) )
      if len(self.files_stats) < FILES_STATS_BUFFER_SIZE:
        return

    slist = ','.join(self.files_stats)

    self.check_connection()
    cursor = self.connection.cursor()
    try:
      try:
        results = cursor.execute(add_files_stats_ignore_repetition % (slist))
      except mysql.connector.Error as err:
        if err.errno == errorcode.ER_DUP_ENTRY:
          # If the error is because the entry is a duplicate we wont't care about it
          pass
        else:
          raise err
    except Exception as err:
      self.logging.error('Failed to insert files stats with info: %s' % (','.join([fileHash, fileBytes, fileLines, fileLOC, fileSLOC, totalTokens, uniqueTokens, tokenHash])) )
      self.logging.error(err)
      sys.exit(1)
    finally:
      cursor.close()
      self.files_stats = []

  def insert_blocks_stats_ignore_repetition(self, blockHash, blockLines, blockLOC, blockSLOC, totalTokens, uniqueTokens, tokenHash, flush = False):
    if not flush:
      self.blocks_stats.append( blocks_stats_list % (blockHash, blockLines, blockLOC, blockSLOC, totalTokens, uniqueTokens, tokenHash) )
      if len(self.blocks_stats) < BLOCKS_STATS_BUFFER_SIZE:
        return

    slist = ','.join(self.blocks_stats)
    self.check_connection()
    cursor = self.connection.cursor()
    try:
      try:
        results = cursor.execute(add_blocks_stats_ignore_repetition % (slist))
      except mysql.connector.Error as err:
        if err.errno == errorcode.ER_DUP_ENTRY:
          # If the error is because the entry is a duplicate we wont't care about it
          pass
        else:
          raise err
    except Exception as err:
      self.logging.error('Failed to insert blocks stats with info: %s' % (','.join([blockHash, blockLines, blockLOC, blockSLOC, totalTokens, uniqueTokens, tokenHash])) )
      self.logging.error(err)
      sys.exit(1)
    finally:
      cursor.close()
      self.blocks_stats = []

  def insert_file(self, file_id, proj_id, file_path, file_url, file_hash, flush = False, autoID = False):
    if not flush:
      if file_url is None:
        file_url = 'NONE'
      self.files.append( files_list % (file_id, proj_id, self.sanitize_string(file_path), self.sanitize_string(file_url), file_hash) )
      if len(self.files) < FILES_BUFFER_SIZE:
        return

    # Prepare the complete list
    if autoID:
      self.files = map(lambda (a, b, c, d, e): (b, c, d, e), self.files)
    flist = ','.join(self.files)

    self.check_connection()
    cursor = self.connection.cursor()
    try:
      if autoID:
        cursor.execute(add_files_autoId % (flist))
      else:
        cursor.execute(add_files % (flist))
      return cursor.lastrowid
    except Exception as err:
      self.logging.error('Failed to insert file %s' % (file_path))
      self.logging.error(err)
      sys.exit(1)
    finally:
      self.file_count += len(self.files)
      self.logging.info("Inserted %s files. Total: %s." % (len(self.files), self.file_count))
      cursor.close()
      self.files = []

  def insert_block(self, project_id, file_id, block_id, block_hash, block_start_line, block_end_line, flush = False):
    if not flush:
      self.blocks.append( blocks_list % (project_id, file_id, block_id, block_hash, block_start_line, block_end_line) )
      if len(self.blocks) < BLOCKS_BUFFER_SIZE:
        return

    # Prepare the complete list
    flist = ','.join(self.blocks)

    self.check_connection()
    cursor = self.connection.cursor()

    try:
      cursor.execute(add_blocks % (flist))
      return cursor.lastrowid
    except Exception as err:
      self.logging.error('Failed to insert block %s' % (block_id))
      self.logging.error(err)
      sys.exit(1)
    finally:
      self.block_count += len(self.blocks)
      self.logging.info("Inserted %s blocks. Total: %s." % (len(self.blocks), self.block_count))
      cursor.close()
      self.blocks = []

  def flush_files_and_stats(self):
    if len(self.files) > 0:
      self.insert_file(None, None, None, None, None, flush = True)
    if len(self.files_stats) > 0:
      self.insert_files_stats_ignore_repetition(None, None, None, None, None, None, None, None, flush = True)

  def flush_blocks_and_stats(self):
    if len(self.blocks) > 0:
      self.insert_block(None, None, None, None, None, None, flush = True)
    if len(self.blocks_stats) > 0:
      self.insert_blocks_stats_ignore_repetition(None, None, None, None, None, None, None, flush = True)

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
  db = DB('pribeiro','CPP','pass',logging)
  #print db.insert_project('la\"\"\"la\"\"\"t,his\'/is/a/pa,th)',None)
  #print db.fileHash_exists('0ee32c85c7df3fe7aa3c858478b0555c')
  #print db.insert_stats_and_is_tokenHash_unique('ssssddsdsddssdd',306,8,4,1,5,5,'37011874e03e4fs77ad44625095103d9')
  #print db.project_exists('\'/data/corpus_8tof/e/b/a/8/7/c/d/c/eba87cdc-d4f8-45f0-bdae-4e7d6253bd5f\'')
  #print db.insert_CCPairs(434,433,34,43)
  #print db.get_max_project_id()
  db.close()

