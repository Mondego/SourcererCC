import sys
from db import DB
import logging


def curate_projects(db, file_mapping_path_url, logging):
    map_path_url = dict()
    with open(file_mapping_path_url, 'r') as file:
        for line in file:
            line = line.strip().split('\t')
            map_path_url[line[0]] = line[1]

    logging.info("Starting curating projects")
    count = 0

    try:
        db.check_connection()
        cursor = db.connection.cursor()
        cursor.execute("SELECT projectId,projectPath,projectUrl FROM projects")
        for pid, ppath, _ in cursor.fetchall():
            if (count % 1000) == 0:
                logging.info("%s projects curated" % count)
                db.connection.commit()
                # db.check_connection()

            # Change below accordingly
            java_path_cut = len('/extra/lopes1/mondego-data/projects/di-stackoverflow-clone/github-repo/java-projects/')

            new_path = ppath[java_path_cut:]
            cursor.execute("UPDATE projects SET projectPath=?, projectUrl=? WHERE projectId=? LIMIT 1", new_path,
                           map_path_url[new_path], pid)
            count += 1
    except Exception as e:
        logging.error('Problem curating projects: %s' % e)
        cursor.close()
        sys.exit(1)
    finally:
        db.connection.commit()
        cursor.close()
        logging.info("Finished. %s projects curated" % count)


def curate_files(db, logging):
    logging.info("Starting curating files")
    count = 0

    try:
        db.check_connection()
        cursor = db.connection.cursor()

        cursor.execute("SELECT MAX(fileId) from files")
        (max_file_id,) = cursor.fetchone()
        ids_range = 0
        logging.info("Max file id for files is %s" % max_file_id)

        while ids_range < max_file_id:
            db.check_connection()

            cursor.execute("SELECT fileId, relativePath FROM files WHERE fileId BETWEEN ? AND ?", ids_range,
                           ids_range + 100000)
            for fid, fpath in cursor.fetchall():
                if (count % 1000) == 0:
                    logging.info("%s files curated" % count)
                    db.connection.commit()

                cut = fpath.find('.zip') + 5
                new_path = fpath[cut:]
                cut = new_path.find('/') + 1
                new_url = new_path[cut:]

                cursor.execute("UPDATE files SET relativePath=?, relativeUrl=? WHERE fileId = ? LIMIT 1", new_path,
                               new_url, fid)

                count += 1
            ids_range += 100000

    except Exception as e:
        logging.error('Problem curating files: %s' % e)
        cursor.close()
        sys.exit(1)
    finally:
        db.connection.commit()
        cursor.close()
        logging.info("Finished. %s files curated" % count)


if __name__ == '__main__':
    host = 'amazon.ics.uci.edu'  # 'localhost' #
    username = 'sourcerer'  # 'user' #
    password = sys.argv[1]
    database = 'DBName'  # WHEN CHANGING THIS, MAKE SURE to change 'file_mapping_path_url' below and 'new_path = ppath[java_path_cut:]' above

    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.DEBUG, format=FORMAT)

    logging.info('__main__')
    db = DB(username, database, password, logging, host)
    logging.info("Connected to %s @ %s" % (database, host))

    file_mapping_path_url = 'java-complete-relativePath-relativeUrl.txt'
    # 'python-relativePath-relativeUrl.txt'
    # 'c++-relativePath-relativeUrl.txt'
    # 'java-relativePath-relativeUrl.txt'
    curate_projects(db, file_mapping_path_url, logging)

    # curate_files(db,logging)

    db.close()
