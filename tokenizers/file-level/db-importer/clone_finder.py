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

def findAllTokenHashClones(files_hashes, files_clones, db_object):
    hashes = files_hashes.values()
    thashes = []
    for th in hashes:
        thashes.append(th['thash'])

    try:
        query = """SELECT fileId, projectId, f.fileHash, tokenHash FROM files as f 
                   JOIN stats as s ON f.fileHash=s.fileHash 
                   WHERE tokenHash in (%s);""" % ("'" + "','".join(thashes) + "'")
        res = db_object.execute(query);
        for f in files_clones.keys():
            thash = files_hashes[f]['thash']
            for (file_id, projectId, fileHash, tokenHash, ) in res:
                if str(file_id) != f and tokenHash == thash:
                    files_clones[f].add((str(file_id), projectId))

    except Exception as e:
        print 'Error on findAllTokenHashClones'
        print e
        sys.exit(1)

def find_clones_for_project(project_id, db_object, debug):
    result = []

    try:
        files_clones = {} # {'file_id' : set(('file_id', project_id),...))
        files_hashes = {} # {'file_id' : {'thash': thash, 'fhash': fhash}}

        query = """SELECT fileId, f.fileHash, tokenHash, totalTokens FROM files as f 
                   JOIN stats as s ON f.fileHash=s.fileHash 
                   WHERE projectId=%s;"""  % (project_id)
        res = db_object.execute(query);
        for (file_id, fileHash, tokenHash, totalTokens, ) in res:
            if (totalTokens > TOKEN_THRESHOLD):
                files_clones.setdefault(str(file_id), set())
                files_hashes.setdefault(str(file_id), {'fhash': fileHash, 'thash': tokenHash})

        total_files = len(res)
        if debug == 'all':
            logging.debug('## Number of files in project %s: %s', project_id, len(files_clones))

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
        findAllTokenHashClones(files_hashes, files_clones, db_object)

        #if debug:
        #    print '## After round 3'
        #    for k, v in files_clones.iteritems():
        #        print k,'-',v

        percentage_host_projects_counter = {}

        clone_set = set()
        for fid, clones in files_clones.iteritems():
            for clone in clones:
                clone_set.add(clone)

        for clone in clone_set:
            projectId = clone[1]
            if percentage_host_projects_counter.has_key(projectId):
                percentage_host_projects_counter[projectId] += 1
            else:
                percentage_host_projects_counter[projectId] = 1

        for k, v in percentage_host_projects_counter.iteritems():
            q = "SELECT COUNT(*) FROM files WHERE projectId = %s;" % (k)
            [(total_files_host, )] = db_object.execute(q)

            percent_cloning = float(v*100)/total_files
            percent_host = float(v*100)/total_files_host
                
            if debug == 'all' or debug == 'final':
                if True:#(percent_cloning > 99) and (str(project_id) != k):
                    print 'Proj',project_id,'in',k,'@',str( float("{0:.2f}".format(percent_cloning)) )+'% ('+str(v)+'/'+str(total_files),'files) affecting', str(float("{0:.2f}".format(percent_host)))+'%','['+str(percentage_cloning_counter[k])+'/'+str(total_files_host),'files]'

            else:
                db_object.insert_projectClones(project_id, v, total_files, float("{0:.2f}".format(percent_cloning)), 
                                               k, v, total_files_host, 
                                               float("{0:.2f}".format(percent_host)))

    except Exception as e:
        print 'Error on find_clones_for_project'
        print e
        traceback.print_exc()
        sys.exit(1)

def start_process(pnum, input_process, DB_user, DB_name, DB_pass):
    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)

    logging.info('Starting process %s', pnum)
    db_object = DB(DB_user, DB_name, DB_pass, logging)

    try:
        pcounter =  0
        for projectId in input_process:
            if pcounter % 500 == 0:
                logging.debug('[%s]: Processing project %s', pnum, projectId)
            find_clones_for_project(projectId, db_object, '') # last field is for debug, and can be 'all','final' or '' (empty)
            pcounter += 1

        db_object.flush_projectClones()


    except Exception as e:
        print 'Error in clone_finder.start_process'
        print e
        sys.exit(1)

    finally:
        db_object.close()

if __name__ == "__main__":
    FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)

    if len(sys.argv) < 4:
        logging.error('Usage: mysql-import.py user passwd database')
        sys.exit(1)

    DB_user  = sys.argv[1]
    DB_pass = sys.argv[2]
    DB_name = sys.argv[3]

    db_object = DB(DB_user, DB_name, DB_pass, logging)

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
                       INDEX(cloneClonedFiles),
                       INDEX(cloneTotalFiles),
                       INDEX(cloneCloningPercent),
                       INDEX(hostId),
                       INDEX(hostAffectedFiles),
                       INDEX(hostTotalFiles),
                       INDEX(hostAffectedPercent)
                       ) ENGINE = MYISAM;"""
        db_object.execute(table)

        # Interesting cases for testing
        #project_id = '42';
        #project_id = '700';
        #project_id = '20000';
        #project_id = '50000';

        pair_number = 0
        commit_interval = 500

        init_time = datetime.datetime.now()
        partial_time = init_time

        logging.info('### Calculating and Importing project clones')

        project_ids = []

        res = db_object.execute("SELECT projectId FROM projects;");
        for (projectId, ) in res:
            project_ids.append(projectId)
            pair_number += 1

        project_ids = [ project_ids[i::N_PROCESSES] for i in xrange(N_PROCESSES) ]

        processes = []
        for process_num in xrange(N_PROCESSES):
            p = Process(name='Process '+str(process_num), target=start_process, args=(process_num, project_ids[process_num], DB_user, DB_name, DB_pass,))
            processes.append(p)
            p.start()

        [p.join() for p in processes]

    except Exception as e:
        print 'Error in clone_finder.__main__'
        print e
        sys.exit(1)

    finally:
        db_object.close()

