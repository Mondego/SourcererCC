#Usage $python this-script.py

import sys, os
import MySQLdb
import collections
import datetime

TOKEN_THRESHOLD = 1

class DB:
    db = None

    def __init__(self, DB_name, DB_user, DB_pass):
        self.DB_name = DB_name
        self.DB_user = DB_user
        self.DB_pass = DB_pass

        self.connect()

    def connect(self):
        try:
            db = MySQLdb.connect(host    = "localhost", # your host, usually localhost
                                 user    = self.DB_user,   # your username
                                 passwd  = self.DB_pass,   # your password
                                 db      = self.DB_name)   # name of the data base

            self.db = db
        except Exception as e:
            print 'Error on DB.connect',e
            print e
            sys.exit(1)

    def commit(self):
        try:
            self.db.commit()
        except Exception as e:
            print 'Error on DB.commit',e
            self.connect()
            self.commit()

    def execute(self, sql_query):
        try:
            cursor = self.db.cursor()
            cursor.execute(sql_query)
            cursor.close()
            return cursor
        except Exception as e:
            print 'Error on DB.execute',e
            self.connect()
            self.execute(sql_query)

    def close(self):
        try:
            self.commit()
            self.db.close()
        except Exception as e:
            print 'Error on DB.close',e
            self.connect()
            self.close()

def getTokenHashClones(fileId,tokenHash,db_object):
    result = set()
    try:
        query = "SELECT fileId FROM files WHERE fileHash IN (SELECT fileHash from stats WHERE tokenHash='%s');" % (tokenHash)
        res = db_object.execute(query);
        for (file_id,) in res:
            result.add(str(file_id))
        result.remove(fileId)
        return result

    except Exception as e:
        print 'Error on getTokenHashClones'
        print e
        sys.exit(1)

def find_clones_for_project(project_id, db_object, debug):
    result = []

    try:
        files_clones = {}
        files_hashes = {}

        query = "SELECT fileId,fileHash FROM files WHERE projectId=%s;" % (project_id)
        res = db_object.execute(query);
        for (file_id,fileHash,) in res:
            files_clones.setdefault(str(file_id), set())
            files_hashes.setdefault(str(file_id), fileHash)

        total_files = len(files_clones.items())
        if debug:
            print '## Number of files in project',project_id,':',len(files_clones)

        # Find CC clones
        for k, v in files_clones.iteritems():
            query = "SELECT pairs.fileId1,fls.fileHash FROM CCPairs as pairs JOIN files as fls ON pairs.fileId1=%s AND pairs.fileId2=fls.fileId;" % (k)
            res = db_object.execute(query);
            for (fileId1,fileHash, ) in res:
                files_clones[k].add(str(fileId1))
                files_hashes.setdefault(str(fileId1), fileHash)

            query = "SELECT pairs.fileId2,fls.fileHash FROM CCPairs as pairs JOIN files as fls ON pairs.fileId2=%s AND pairs.fileId1=fls.fileId;" % (k)
            res = db_object.execute(query);
            for (fileId2,fileHash, ) in res:
                files_clones[k].add(str(fileId2))
                files_hashes.setdefault(str(fileId2), fileHash)

        if debug:
            print '## After round 1'
            for k, v in files_clones.iteritems():
                if len(v) > 0:
                    print k,'-',v
            #for k, v in files_hashes.iteritems():
            #    print k,'-',v

        # File hashes to token-hashes and filter small files
        for k, v in files_hashes.iteritems():
            query = "SELECT tokenHash,totalTokens FROM stats WHERE fileHash='%s';" % (files_hashes[k])
            res = db_object.execute(query)
            for (tokenHash,totalTokens ) in res:
                if totalTokens >= TOKEN_THRESHOLD:
                    files_hashes[k] = tokenHash
                else:
                    files_hashes[k] = None

        #if debug:
        #    print '## After round 2'
        #    for k, v in files_clones.iteritems():
        #        print k,'-',v
        #    for k, v in files_hashes.iteritems():
        #        print k,'-',v

        # Find token-hash clones
        for k, v in files_clones.iteritems():
            set_files = v
            for CCClone in v:
               #print CCClone,'-',getTokenHashClones(CCClone,files_hashes[CCClone],db)
                if files_hashes[CCClone] is not None:
                    set_files = set_files | getTokenHashClones(CCClone,files_hashes[CCClone],db_object)
            
            if files_hashes[k] is not None:
                set_files = set_files | getTokenHashClones(k,files_hashes[k],db_object)

            files_clones[k] = set_files

        #if debug:
        #    print '## After round 3'
        #    for k, v in files_clones.iteritems():
        #        print k,'-',v

        # Transform fileId into projectId, so we will know for
        #   each file k in which project it is cloned
        percentage_cloning_counter = {} # This list might have repetition of projectId,
                                   # but will have at most one projectId per file

        percentage_host_projects_counter = {}

        all_files_temp_set = set()
        # From file ids to project ids
        for k, v in files_clones.iteritems():
            all_files_temp_set |= set(v)

            if len(v) > 0:
                query = "SELECT projectId FROM files WHERE fileId IN (%s);" % ( ','.join(v) )
                res = db_object.execute(query)
                aux = set()
                for (projectId, ) in res:
                    aux.add(str(projectId))

                # Counter on aux
                for proj in aux:
                    if percentage_cloning_counter.has_key(proj):
                        percentage_cloning_counter[proj] = percentage_cloning_counter[proj] + 1
                    else:
                        percentage_cloning_counter[proj] = 1

        if len(all_files_temp_set) > 0:
            query = "SELECT projectId FROM files WHERE fileId IN (%s);" % ( ','.join(all_files_temp_set) )
            res = db_object.execute(query)
            for (projectId, ) in res:
                projectId = str(projectId)
                if percentage_host_projects_counter.has_key(projectId):
                    percentage_host_projects_counter[projectId] = percentage_host_projects_counter[projectId] + 1
                else:
                    percentage_host_projects_counter[projectId] = 1

        for k, v in percentage_cloning_counter.iteritems():
            q = "SELECT COUNT(*) FROM files WHERE projectId = %s;" % (k)
            res = db_object.execute(q)

            (total_files_host, ) = db_object.fetchone()
            percent_cloning = float(v*100)/total_files
            percent_host = float(percentage_host_projects_counter[k]*100)/total_files_host

            if debug:
                if True:#(percent_cloning > 99) and (str(project_id) != k):
                    print 'Proj',project_id,'in',k,'@',str( float("{0:.2f}".format(percent_cloning)) )+'% ('+str(v)+'/'+str(total_files),'files) affecting', str(float("{0:.2f}".format(percent_host)))+'%','['+str(percentage_cloning_counter[k])+'/'+str(total_files_host),'files]'

            if not debug:
                query = "INSERT INTO projectClones (cloneId,cloneClonedFiles,cloneTotalFiles,cloneCloningPercent,hostId,hostAffectedFiles,hostTotalFiles,hostAffectedPercent) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);" % (project_id, v, total_files, float("{0:.2f}".format(percent_cloning)), k, percentage_cloning_counter[k], total_files_host, float("{0:.2f}".format(percent_host)))
                db_object.execute(query)
                #cursor.execute("""INSERT INTO projectClones (cloneId,cloneClonedFiles,cloneTotalFiles,cloneCloningPercent,hostId,hostAffectedFiles,hostTotalFiles,hostAffectedPercent) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);""",
                #           (project_id, v, total_files, float("{0:.2f}".format(percent_cloning)), k, percentage_cloning_counter[k], total_files_host, float("{0:.2f}".format(percent_host))) )

        #    
        #if debug:
        #    query = "SELECT projectUrl FROM projects WHERE projectId IN ("+str(project_id)+","+str(k)+");"
        #    cursor.execute(query)
        #    for (projectUrl, ) in cursor:
        #        print projectUrl

        #if debug:
        #    print '## After round 3'
        #    for k, v in files_clones.iteritems():
        #        print k,'-',v

    except Exception as e:
        print 'Error on find_clones_for_project'
        print e
        sys.exit(1)

if __name__ == "__main__":

    DB_name = sys.argv[1]
    DB_user = 'pribeiro'
    DB_pass = 'pass'

    db_object = DB(DB_name, DB_user, DB_pass)

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

        print '### Calculating and Importing project clones'

        res = db_object.execute("SELECT projectId FROM projects");
        for (projectId, ) in res:
            find_clones_for_project(projectId,db_object,False)
            pair_number += 1

            if pair_number%commit_interval == 0:
                db_object.commit()
                print '    ',pair_number,'projects calculated and info commited in',datetime.datetime.now() - partial_time
                partial_time = datetime.datetime.now()

        print '    all ',pair_number,'projects calculated and info commited in',datetime.datetime.now() - init_time

        #find_clones_for_project(597,db,True)

    except Exception as e:
        print 'Error in clone_finder.__main__'
        print e
        sys.exit(1)

    finally:
        db_object.close()

