import sys, os
from db import DB
import logging

SAMPLE_SIZE = 50
DB_user = 'pribeiro'
DB_pass = 'pass'
DB_name = 'Java'


FORMAT = '[%(levelname)s] (%(asctime)-15s) %(message)s'
logging.basicConfig(level=logging.DEBUG,format=FORMAT)


logging.info("""Initializing DB '%s' with '%s:%s'. Sample size: %s""" % (DB_name,DB_user,DB_pass,SAMPLE_SIZE))
db = DB(DB_user,DB_name,DB_pass,logging)


logging.info('Top hosting projects')
c = db.execute('SELECT pjs.projectId, pjs.projectUrl, COUNT(clones.hostId) FROM projects AS pjs JOIN projectClones AS clones ON pjs.projectId = clones.hostId AND clones.cloneCloningPercent >= 80 GROUP BY clones.hostId ORDER BY COUNT(clones.hostId) DESC LIMIT %s;' % SAMPLE_SIZE)
with open('top-hosting-projects.csv','w') as file:
    file.write('projectId,projectUrl,count\n')
    for (projectId, projectUrl,count ) in c:
        file.write("""%s,%s,%s\n""" % (projectId, projectUrl, count))


logging.info('Top cloned projects')
c = db.execute('SELECT pjs.projectId, pjs.projectUrl, COUNT(clones.cloneId) FROM projects AS pjs JOIN projectClones AS clones ON pjs.projectId = clones.cloneId AND clones.cloneCloningPercent >= 80 GROUP BY clones.cloneId ORDER BY COUNT(clones.cloneId) DESC LIMIT %s;' % SAMPLE_SIZE)
with open('top-cloned-projects.csv','w') as file:
    file.write('projectId,projectUrl,count\n')
    for (projectId, projectUrl,count ) in c:
        file.write("""%s,%s,%s\n""" % (projectId, projectUrl, count))


#logging.info('Sampling CCPairs')
#c = db.execute('SELECT pairs.fileId1,fls.relativeUrl,pairs.fileId2,fls2.relativeUrl FROM CCPairs AS pairs JOIN files as fls JOIN files as fls2 ON pairs.fileId1=fls.fileId AND pairs.fileId2=fls2.fileId ORDER BY RAND() LIMIT %s;' % SAMPLE_SIZE)
#with open('sample-CCPairs.csv','w') as file:
#    file.write('fileId1,fileUrl1,fileId2,fileUrl2\n')
#    for (fileId1,fileUrl1,fileId2,fileUrl2, ) in c:
#        file.write("""%s,%s,%s,%s\n""" % (fileId1,fileUrl1,fileId2,fileUrl2))


#logging.info('Sampling unique files')
#c = db.execute('SELECT fileId, relativeUrl from files WHERE filehash IN (SELECT fileHash FROM stats WHERE tokenHash IN (SELECT DISTINCT(tokenHash) FROM stats)) ORDER BY RAND() LIMIT %s;' % SAMPLE_SIZE)
#with open('sample-unique-files.csv','w') as file:
#    file.write('fileId,fileUrl\n')
#    for (fileId, relativeUrl, ) in c:
#        file.write("""%s,%s\n""" % (fileId, relativeUrl))

logging.info('Sampling project-clone pairs')
c = db.execute('SELECT projs.projectUrl,projs2.projectUrl,clones.cloneCloningPercent,clones.cloneClonedFiles,clones.hostAffectedPercent,clones.hostAffectedFiles FROM projects AS projs JOIN projectClones AS clones JOIN projects AS projs2 ON clones.cloneId <> clones.hostId AND clones.cloneId = projs.projectId AND clones.hostId = projs2.projectId AND clones.cloneCloningPercent >+ 80 ORDER BY RAND() LIMIT %s;' % SAMPLE_SIZE)
with open('sample-project-pairs.csv','w') as file:
    file.write('projectUrl1,projectUrl2,cloneCloningPercent,cloneClonedFiles,hostAffectedPercent,hostAffectedFiles\n')
    for (projectUrl1,projectUrl2,cloneCloningPercent,cloneClonedFiles,hostAffectedPercent,hostAffectedFiles, ) in c:
        file.write("""%s,%s,%s,%s,%s,%s\n""" % (projectUrl1,projectUrl2,cloneCloningPercent,cloneClonedFiles,hostAffectedPercent,hostAffectedFiles))


logging.info('Sampling non-cloning projects')
c = db.execute('SELECT projectId, projectUrl FROM projects WHERE projectId IN (SELECT * FROM ((SELECT cloneId AS name FROM projectClones WHERE cloneCloningPercent >= 80) UNION ALL (SELECT hostId AS name FROM projectClones WHERE cloneCloningPercent >= 80)) AS a) ORDER BY RAND() LIMIT %s;' % SAMPLE_SIZE)
with open('sample-non-cloning-projects.csv','w') as file:
    file.write('projectId,projectUrl\n')
    for (projectId, projectUrl, ) in c:
        file.write("""%s,%s\n""" % (projectId, projectUrl))



#logging.info('1 file from top token-equal groups.')
#c1 = db.execute('SELECT tokenHash, COUNT(tokenHash) FROM stats GROUP BY tokenHash ORDER BY COUNT(tokenHash) DESC LIMIT %s;' % SAMPLE_SIZE)
#c2 = db.execute('SELECT fls.fileId, fls.relativeUrl FROM files AS fls JOIN stats AS sts ON fls.fileHash=sts.fileHash AND sts.tokenHash IN (SELECT tokenHash FROM stats GROUP BY tokenHash ORDER BY COUNT(tokenHash) DESC) LIMIT %s;' % SAMPLE_SIZE)
#counts = []
#for (tokenHash,count, ) in c1:
#    counts = counts + [count]
#with open('top-hashToken-groups.csv','w') as file:
#    file.write('fileId,fileUrl\n')
#    for (fileId, fileUrl, ) in c2:
#        file.write("""%s,%s,%s\n""" % (fileId, fileUrl, counts.pop()) )



