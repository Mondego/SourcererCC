import sys, os
from db import DB
import logging

def curate_projects(db,file_mapping_path_url,logging):
  map_path_url = dict()
  with open(file_mapping_path_url,'r') as file:
    for line in file:
      line = line.strip().split('\t')
      map_path_url[line[0]] = line[1]

  logging.info("Starting curating projects")
  count = 0

  try:
    db.check_connection()
    cursor = db.connection.cursor()
    cursor.execute("SELECT projectId,projectPath,projectUrl FROM projects")
    for pid,ppath,purl in cursor.fetchall():
      if (count % 1000) == 0:
        logging.info("%s projects curated" % count)
        db.connection.commit()
        #db.check_connection()

      ## Change below accordingly
      java_path_cut   = len('/extra/lopes1/mondego-data/projects/di-stackoverflow-clone/github-repo/java-projects/')
      cpp_path_cut    = len('/extra/lopes1/mondego-data/projects/di-stackoverflow-clone/github-repo/c++-projects/')
      python_path_cut = len('/extra/lopes1/mondego-data/projects/di-stackoverflow-clone/github-repo/python-projects/')

      new_path = ppath[python_path_cut:]
      cursor.execute("UPDATE projects SET projectPath=\"%s\", projectUrl=\"%s\" WHERE projectId=%s LIMIT 1" % (new_path,map_path_url[new_path],pid))
      #print "UPDATE projects SET projectPath=\"%s\", projectUrl=\"%s\" WHERE projectId=%s LIMIT 1" % (new_path,map_path_url[new_path],pid)
      count += 1
  except Exception as e:
    logging.error('Problem curating projects: %s' % (e))
    cursor.close()
    sys.exit(1)
  finally:
    db.connection.commit()
    cursor.close()
    logging.info("Finished. %s projects curated" % count)

def curate_files(db,logging):
  logging.info("Starting curating files")
  count = 0

  try:
    db.check_connection()
    cursor = db.connection.cursor()

    cursor.execute("SELECT MAX(fileId) from files")
    (max_file_id,) = cursor.fetchone()
    ids_range = 0
    logging.info("Max file id for files is %s" % (max_file_id))

    while(ids_range < max_file_id):
      db.check_connection()

      cursor.execute("SELECT fileId,projectId,relativePath,relativeUrl,fileHash FROM files WHERE fileId BETWEEN %s AND %s" % (ids_range,ids_range+100000))
      for fid,pid,fpath,furl,fhash in cursor.fetchall():
        if (count % 1000) == 0:
          logging.info("%s files curated" % count)
          db.connection.commit()

        cut = fpath.find('.zip') + 5
        new_path = fpath[cut:]
        cut = new_path.find('/') + 1
        new_url = new_path[cut:]

        cursor.execute("UPDATE files SET relativePath=\"%s\", relativeUrl=\"%s\" WHERE fileId=%s LIMIT 1" % (new_path,new_url,fid))
        #print fid,pid,new_path,new_url,fhash

        count += 1
      ids_range += 100000

  except Exception as e:
    logging.error('Problem curating files: %s' % (e))
    cursor.close()
    sys.exit(1)
  finally:
    db.connection.commit()
    cursor.close()
    logging.info("Finished. %s files curated" % count)


if __name__ == '__main__':

  host     = 'amazon.ics.uci.edu'#'localhost' #
  username = 'sourcerer'#'user' #
  password = sys.argv[1]
  database = 'CPP2'#'clonestesting' #

  FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
  logging.basicConfig(level=logging.DEBUG,format=FORMAT)

  logging.info('__main__')
  db = DB(username, database, password, logging, host)
  logging.info("Connected to %s @ %s" % (database,host))

  #file_mapping_path_url = 'python-relativePath-relativeUrl.txt'
  # 'c++-relativePath-relativeUrl.txt'
  # 'java-relativePath-relativeUrl.txt'
  #curate_projects(db,file_mapping_path_url,logging)

  curate_files(db,logging)

  db.close()

