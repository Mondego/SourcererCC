#Usage $python this-script.py

import sys, os
import collections
import datetime
from db import DB
import logging
import multiprocessing as mp
from multiprocessing import Process, Value
import traceback

TOKEN_THRESHOLD = 1
N_PROCESSES = 2

def findAllTokenHashClones(project_id, token_hashes, files_clones, db_object):

    try:
        query = """SELECT fileId, projectId, f.fileHash, tokenHash FROM files as f 
                   JOIN stats as s ON f.fileHash=s.fileHash 
                   WHERE tokenHash in (%s) AND projectId >= %s;""" % ("'" + "','".join(token_hashes.keys()) + "'", project_id)
        res = db_object.execute(query);
        for (file_id, projectId, fileHash, tokenHash, ) in res:
            pfiles = token_hashes[tokenHash]
            for f in pfiles:
                if str(file_id) != str(f):
                    files_clones[f].add((str(file_id), projectId))

    except Exception as e:
        print 'Error on findAllTokenHashClones'
        print e
        sys.exit(1)

def find_clones_for_project(project_id, project_file_counts, db_object, debug):
    result = []

    try:
        files_clones = {} # {'file_id' : set(('file_id', project_id),...))
        files_hashes = {} # {'file_id' : {'thash': thash, 'fhash': fhash}}
        token_hashes = {} # {token_hash : [file_id, file_id, ...]}, all files within this project 

        query = """SELECT fileId, f.fileHash, tokenHash, totalTokens FROM files as f 
                   JOIN stats as s ON f.fileHash=s.fileHash 
                   WHERE projectId=%s;"""  % (project_id)
        res = db_object.execute(query);
        for (file_id, fileHash, tokenHash, totalTokens, ) in res:
            if (totalTokens > TOKEN_THRESHOLD):
                files_clones.setdefault(str(file_id), set())
                files_hashes.setdefault(str(file_id), {'fhash': fileHash, 'thash': tokenHash})
                if tokenHash not in token_hashes:
                    token_hashes[tokenHash] = []
                token_hashes[tokenHash].append(str(file_id))

        total_files = len(res)
        #logging.debug('## Number of files in project %s: %s', project_id, total_files)

        # # Find CC clones
        # for k, v in files_clones.iteritems():
        #     query = "SELECT pairs.fileId1,fls.fileHash FROM CCPairs as pairs JOIN files as fls ON pairs.fileId1=%s AND pairs.fileId2=fls.fileId;" % (k)
        #     res = db_object.execute(query);
        #     for (fileId1,fileHash, ) in res:
        #         files_clones[k].add(str(fileId1))
        #         files_hashes.setdefault(str(fileId1), fileHash)

        #     query = "SELECT pairs.fileId2,fls.fileHash FROM CCPairs as pairs JOIN files as fls ON pairs.fileId2=%s AND pairs.fileId1=fls.fileId;" % (k)
        #     res = db_object.execute(query);
        #     for (fileId2,fileHash, ) in res:
        #         files_clones[k].add(str(fileId2))
        #         files_hashes.setdefault(str(fileId2), fileHash)

        if debug == 'all':
            logging.debug('## After round 1')
            for k, v in files_clones.iteritems():
                if len(v) > 0:
                    logging.debug('%s-%s', k, v)
            #for k, v in files_hashes.iteritems():
            #    print k,'-',v

        #if debug:
        #    print '## After round 2'
        #    for k, v in files_clones.iteritems():
        #        print k,'-',v
        #    for k, v in files_hashes.iteritems():
        #        print k,'-',v

        # Find token-hash clones
        findAllTokenHashClones(project_id, token_hashes, files_clones, db_object)

        #if debug:
        #    print '## After round 3'
        #    for k, v in files_clones.iteritems():
        #        print k,'-',v

        percentage_clone_projects_counter = {}
        percentage_host_projects_counter = {}

        project_file_set = {}
        clone_set = set()
        for fid, clones in files_clones.iteritems():
            project_counted = False
            for clone in clones:
                projectId = clone[1]
                clone_set.add(clone)
                if projectId not in project_file_set:
                    project_file_set[projectId] = set()
                project_file_set[projectId].add(fid)
                
        # How many of this project's files are present in each of the other project?
        for pid, file_list in project_file_set.iteritems():
            percentage_clone_projects_counter[pid] = len(file_list)


        # How many of the other projects files are present in this project?
        for clone in clone_set:
            projectId = clone[1]
            if percentage_host_projects_counter.has_key(projectId):
                percentage_host_projects_counter[projectId] += 1
            else:
                percentage_host_projects_counter[projectId] = 1

        if len(percentage_host_projects_counter) > 0:
            # The key k (projects) should be the same between 
            # percentage_clone_projects_counter and percentage_host_projects_counter
            for k, v in percentage_host_projects_counter.iteritems():
                
                percent_cloning = float(percentage_clone_projects_counter[k]*100)/total_files
                percent_host = float(v*100)/project_file_counts[k]
                
                # Don't store insignificant clones
                if percent_cloning < 50 and percent_host < 50:
                    continue

                if debug == 'all' or debug == 'final':
                    if True:#(percent_cloning > 99) and (str(project_id) != k):
                        print 'Proj',project_id,'in',k,'@',str( float("{0:.2f}".format(percent_cloning)) )+'% ('+str(v)+'/'+str(total_files),'files) affecting', str(float("{0:.2f}".format(percent_host)))+'%','['+str(percentage_cloning_counter[k])+'/'+str(total_files_host),'files]'

                else:
                    db_object.insert_projectClones(project_id, percentage_clone_projects_counter[k], total_files, float("{0:.2f}".format(percent_cloning)), 
                                                   k, v, project_file_counts[k], 
                                                   float("{0:.2f}".format(percent_host)))

    except Exception as e:
        print 'Error on find_clones_for_project'
        print e
        traceback.print_exc()
        sys.exit(1)

def load_project_file_counts(db_object, project_file_counts):
    logging.debug("Loading project file counts...")
    q = "SELECT projectId, COUNT(*) FROM files GROUP BY projectId;" 
    res = db_object.execute(q)
    for (pid, total_files_host, ) in res:
        project_file_counts[pid] = total_files_host
    logging.debug("Loading project file counts... done")


def start_process(pnum, input_process, DB_user, DB_name, DB_pass, project_file_counts, host):
    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)

    logging.info('Starting process %s', pnum)
    db_object = DB(DB_user, DB_name, DB_pass, logging, host)

    try:
        pcounter =  0
        for projectId in input_process:
            if pcounter % 50 == 0:
                logging.debug('[%s]: Processing project %s (%s)', pnum, pcounter, projectId)
            find_clones_for_project(projectId, project_file_counts, db_object, '') # last field is for debug, and can be 'all','final' or '' (empty)
            pcounter += 1

        db_object.flush_projectClones()

    except Exception as e:
        print 'Error in clone_finder.start_process'
        print e
        sys.exit(1)

    finally:
        db_object.close()

if __name__ == "__main__":
    log_path = 'LOG-db-clonefinder.log'

    if os.path.isfile(log_path):
        print 'ERROR: Log file:',log_path,'already exists'
        sys.exit(1)

    FORMAT = '[%(levelname)s] (%(threadName)s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)
    file_handler = logging.FileHandler(log_path)
    file_handler.setFormatter(logging.Formatter(FORMAT))
    logging.getLogger().addHandler(file_handler)

    if len(sys.argv) < 4:
        logging.error('Usage: clone-finder.py user passwd database (host|OPTIONAL)') 
        sys.exit(1)

    DB_user  = sys.argv[1]
    DB_pass = sys.argv[2]
    DB_name = sys.argv[3]
    host = 'localhost'
    if len(sys.argv) == 5:
        host = sys.argv[4]

    db_object = DB(DB_user, DB_name, DB_pass, logging, host)

    try:
        db_object.execute("DROP TABLE IF EXISTS `projectClones`;")
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
                       INDEX(cloneTotalFiles),
                       INDEX(cloneCloningPercent),
                       INDEX(hostId),
                       INDEX(hostTotalFiles),
                       INDEX(hostAffectedPercent),
                       UNIQUE INDEX pair (cloneId, hostId)
                       ) ENGINE = MYISAM;"""
        db_object.execute(table)

        project_file_counts = {}
        load_project_file_counts(db_object, project_file_counts)

        pair_number = 0
        commit_interval = 500

        init_time = datetime.datetime.now()
        partial_time = init_time

        logging.info('### Calculating and Importing project clones')

        project_ids = []

        for projectId in project_file_counts.keys():
            project_ids.append(projectId)
            pair_number += 1

        project_ids = [ project_ids[i::N_PROCESSES] for i in xrange(N_PROCESSES) ]

        processes = []
        for process_num in xrange(N_PROCESSES):
            p = Process(name='Process '+str(process_num), target=start_process, 
                        args=(process_num, project_ids[process_num], DB_user, DB_name, DB_pass, project_file_counts, host, ))
            processes.append(p)
            p.start()

        [p.join() for p in processes]

    except Exception as e:
        print 'Error in clone_finder.__main__'
        print e
        sys.exit(1)

    finally:
        db_object.close()

