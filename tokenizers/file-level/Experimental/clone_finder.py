#Usage $python this-script.py

import sys, os
import MySQLdb
import collections
import datetime

def getTokenHashClones(fileId,tokenHash,db):
    result = set()
    try:
        cursor = db.cursor()
        query = "SELECT fileId FROM files WHERE fileHash IN (SELECT fileHash from stats WHERE tokenHash='%s');" % (tokenHash)
        cursor.execute(query);
        for (file_id,) in cursor:
            result.add(str(file_id))

        return result

    except Exception as e:
        print 'Error on getTokenHashClones'
        print e
        sys.exit(1)

    finally:
        db.commit()
        cursor.close()

def find_clones_for_project(project_id, db, debug):
    try:
        cursor = db.cursor()
        files_clones = {}
        files_hashes = {}

        query = "SELECT fileId,fileHash FROM files WHERE projectId=%s;" % (project_id)
        cursor.execute(query);
        for (file_id,fileHash,) in cursor:
            files_clones.setdefault(str(file_id), set())
            files_hashes.setdefault(str(file_id), fileHash)

        total_files = len(files_clones.items())
        if debug:
            print '## Number of files in project',project_id,':',len(files_clones)

        # Find CC clones
        for k, v in files_clones.iteritems():
            query = "SELECT pairs.fileId1,fls.fileHash FROM CCPairs as pairs JOIN files as fls ON pairs.fileId2=%s AND pairs.fileId2=fls.fileId;" % (k)
            cursor.execute(query);
            for (fileId1,fileHash, ) in cursor:
                files_clones[k].add(str(fileId1))
                files_hashes.setdefault(str(fileId1), fileHash)

            query = "SELECT pairs.fileId2,fls.fileHash FROM CCPairs as pairs JOIN files as fls ON pairs.fileId1=%s AND pairs.fileId1=fls.fileId;" % (k)
            cursor.execute(query);
            for (fileId2,fileHash, ) in cursor:
                files_clones[k].add(str(fileId2))
                files_hashes.setdefault(str(fileId2), fileHash)

        if debug:
            print '## After round 1'
            for k, v in files_clones.iteritems():
                print k,'-',v
            #for k, v in files_hashes.iteritems():
            #    print k,'-',v

        # File hashes to token-hashes
        for k, v in files_hashes.iteritems():
            query = "SELECT tokenHash FROM stats WHERE fileHash='%s';" % (files_hashes[k])
            cursor.execute(query);
            for (tokenHash, ) in cursor:
                files_hashes[k] = tokenHash

        if debug:
            print '## After round 2'
            for k, v in files_clones.iteritems():
                print k,'-',v
            for k, v in files_hashes.iteritems():
                print k,'-',v

        # Find token-hashe clones
        for k, v in files_clones.iteritems():
            set_files = v
            for CCClone in v:
                #print CCClone,'-',getTokenHashClones(CCClone,files_hashes[CCClone],db)
                set_files = set_files | getTokenHashClones(CCClone,files_hashes[CCClone],db)
            
            set_files = set_files | getTokenHashClones(k,files_hashes[k],db)
            set_files.remove(k)
            files_clones[k] = set_files

        if debug:
            print '## After round 3'
            for k, v in files_clones.iteritems():
                print k,'-',v

        # Transform fileId into projectId, so we will know for
        #   each file k in which project it is cloned
        list_project_ids = {} # This list might have repetition of projectId,
                              # but will have at most one projectId per file
        hosting_project_affected = {} # With this list we will calculate the number of
                                      # files in the hosting projected 'affected' by cloning
    
        for k, v in files_clones.iteritems():
            if len(v) > 0:
                query = "SELECT projectId FROM files WHERE fileId IN (%s);" % ( ','.join(v) )
                cursor.execute(query)
                aux = set()
                for (projectId, ) in cursor:
                    aux.add(projectId)
                    if hosting_project_affected.has_key(projectId):
                        hosting_project_affected[projectId] = hosting_project_affected[projectId] + 1
                    else:
                        hosting_project_affected[projectId] = 1
                for proj in aux:
                    if list_project_ids.has_key(proj):
                        list_project_ids[proj] = list_project_ids[proj] + 1
                    else:
                        list_project_ids[proj] = 1
    
        for k, v in list_project_ids.iteritems():
            percent_cloning = float(v*100)/total_files
            
            q = "SELECT COUNT(*) FROM files WHERE projectId = %s;" % (k)
            cursor.execute(q)
            (total_files_host, ) = cursor.fetchone()

            percent_host = float(hosting_project_affected[k]*100)/total_files_host

            if debug:
                print 'Proj',project_id,'in',k,'@',str( float("{0:.2f}".format(percent_cloning)) )+'% (',v,'/',total_files,'files ) affecting', str(float("{0:.2f}".format(percent_host)))+'%','[',hosting_project_affected[k],'/',total_files_host,'files ]'

            cursor.execute("""INSERT INTO projectClones (cloneId,cloneClonedFiles,cloneTotalFiles,cloneCloningPercent,hostId,hostAffectedFiles,hostTotalFiles,hostAffectedPercent) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);""",
                            (project_id, v, total_files, float("{0:.2f}".format(percent_cloning)), k, hosting_project_affected[k], total_files_host, float("{0:.2f}".format(percent_host))) )
            if debug:
                query = "SELECT projectUrl FROM projects WHERE projectId IN ("+str(project_id)+","+str(k)+");"
                cursor.execute(query)
                for (projectUrl, ) in cursor:
                    print projectUrl

    except Exception as e:
        print 'Error on find_clones_for_project'
        print e
        sys.exit(1)

    finally:
        db.commit()
        cursor.close()


if __name__ == "__main__":

    DB_name = sys.argv[1]
    DB_user = 'pribeiro'
    DB_pass = 'pass'

    try:
        db = MySQLdb.connect(host="localhost", # your host, usually localhost
                             user=  DB_user,   # your username
                             passwd=DB_pass,   # your password
                             db=    DB_name)   # name of the data base

        cursor = db.cursor()

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
        cursor.close()

        # Interesting cases for testing
        #project_id = '42';
        #project_id = '700';
        #project_id = '20000';
        #project_id = '50000';

        pair_number = 0
        commit_interval = 100

        init_time = datetime.datetime.now()
        partial_time = init_time

        print '### Calculating and Importing project clones'
        cursor = db.cursor()
        cursor.execute("SELECT projectId FROM projects");
        for (projectId, ) in cursor:
            find_clones_for_project(projectId,db,False)
            pair_number += 1
            if pair_number%commit_interval == 0:
                db.commit()
                print '    ',pair_number,'projects calculated and info commited in',datetime.datetime.now() - partial_time
                partial_time = datetime.datetime.now()
        print '    all ',pair_number,'projects calculated and info commited in',datetime.datetime.now() - init_time
        cursor.close()

#        find_clones_for_project(42,db,True)
#
    except Exception as e:
        print 'Error accessing Database'
        print e
        sys.exit(1)

    finally:
        db.commit()
        db.close()

#
#    # Transform fileId into projectId, so we will know for
#    #   each file k in which project it is cloned
#    list_project_ids = {}# This list might have repetition of projectId,
#                          # but will have at most one projectId per file
#    hosting_project_affected = {} # With this list we will calculate the number of
#                                  # files in the hosting projected 'affected' by cloning
#
#    for k, v in files_clones.iteritems():
#        if len(v) > 0:
#            query = "SELECT projectId FROM filesStats WHERE fileId IN (%s);" % ( ','.join(v) )
#            cursor.execute(query)
#            aux = set()
#            for (projectId, ) in cursor:
#                aux.add(projectId)
#                if hosting_project_affected.has_key(projectId):
#                    hosting_project_affected[projectId] = hosting_project_affected[projectId] + 1
#                else:
#                    hosting_project_affected[projectId] = 1
#            for proj in aux:
#                if list_project_ids.has_key(proj):
#                    list_project_ids[proj] = list_project_ids[proj] + 1
#                else:
#                    list_project_ids[proj] = 1
#            #list_project_ids += list(aux)
#
#    #if debug:
#    for k, v in list_project_ids.iteritems():
#        percent_cloning = float(v*100)/total_files
#        
#        #if percent_cloning > 80:
#
#        total_files_host = 0
#        query = "SELECT fileId FROM filesStats WHERE projectId = %s;" % (k)
#        cursor.execute(query);
#        for (file_id,) in cursor:
#            total_files_host += 1
#        percent_host = float(hosting_project_affected[k]*100)/total_files_host
#
#        if debug:
#            print 'Proj',project_id,'in',k,'@',str( float("{0:.2f}".format(percent_cloning)) )+'% (',v,'/',total_files,'files ) affecting', str(float("{0:.2f}".format(percent_host)))+'%','[',hosting_project_affected[k],'/',total_files_host,'files ]'
#
#        cursor.execute("""INSERT INTO projectClones (cloneId,cloneClonedFiles,cloneTotalFiles,cloneCloningPercent,hostId,hostAffectedFiles,hostTotalFiles,hostAffectedPercent) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);""",
#                        (project_id, v, total_files, float("{0:.2f}".format(percent_cloning)), k, hosting_project_affected[k], total_files_host, float("{0:.2f}".format(percent_host))) )
#        if debug:
#            query = "SELECT projectUrl FROM projects WHERE projectId IN ("+str(project_id)+","+str(k)+");"
#            cursor.execute(query)
#            for (projectUrl, ) in cursor:
#                print projectUrl



#if __name__ == "__main__":
#    try:
#        db = MySQLdb.connect(host   = "localhost",    # your host, usually localhost
#                             user   = "USER",     # your username
#                             passwd = "PASS",         # your password
#                             db     = "JAVA") # name of the data base
#    except:
#        print 'Error accessing Database'
#        sys.exit(1)
#
#    cursor = db.cursor()
#    print '### Creating Table projectClones'
#    # CREATE filesTokens Database
#    cursor.execute("DROP TABLE IF EXISTS `projectClones`;")
#    ccPairs = """ CREATE TABLE `projectClones` (
#                    id INT(9) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
#                    cloneId INT(7) UNSIGNED NOT NULL,
#                    cloneClonedFiles INT(7) UNSIGNED NOT NULL,
#                    cloneTotalFiles INT(7) UNSIGNED NOT NULL,
#                    cloneCloningPercent FLOAT NOT NULL,
#                    hostId INT(7) UNSIGNED NOT NULL,
#                    hostAffectedFiles INT(7) UNSIGNED NOT NULL,
#                    hostTotalFiles INT(7) UNSIGNED NOT NULL,
#                    hostAffectedPercent FLOAT NOT NULL,
#                    INDEX(cloneId),
#                    INDEX(cloneClonedFiles),
#                    INDEX(cloneTotalFiles),
#                    INDEX(cloneCloningPercent),
#                    INDEX(hostId),
#                    INDEX(hostAffectedFiles),
#                    INDEX(hostTotalFiles),
#                    INDEX(hostAffectedPercent)
#                    ) ENGINE = MYISAM; """
#    cursor.execute(ccPairs)
#    db.commit()
#
#
#    #project_id = '42';
#    #project_id = '700';
#    #project_id = '20000';
#    #project_id = '50000';
#
#    pair_number = 0
#    commit_interval = 100
#
#    init_time = datetime.datetime.now()
#    partial_time = init_time
#
#    print '### Calculating and Importing project clones'
#    cursor.execute("SELECT projectId FROM projects");
#    for (projectId, ) in cursor:
#        find_clones_for_project(projectId,cursor,False)
#        pair_number += 1
#        if pair_number%commit_interval == 0:
#            db.commit()
#            print '    ',pair_number,'projects calculated and info commited in',datetime.datetime.now() - partial_time
#            partial_time = datetime.datetime.now()
#    db.commit()
#    print '    all ',pair_number,'projects calculated and info commited in',datetime.datetime.now() - init_time

