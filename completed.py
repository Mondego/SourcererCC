'''
Created on Apr 11, 2016

@author: saini
'''

def tail(f, n, offset=0):
    # print "tailing", f, n
    stdin, stdout = os.popen2("tail -n " + n + offset + " " + f)
    stdin.close()
    lines = stdout.readlines(); stdout.close()
    return lines[:, -offset]


def tail2(f, lines=10):
    # print ("tailing ")
    total_lines_wanted = lines

    BLOCK_SIZE = 1024
    f.seek(0, 2)
    block_end_byte = f.tell()
    lines_to_go = total_lines_wanted
    block_number = -1
    blocks = []  # blocks of size BLOCK_SIZE, in reverse order starting
                # from the end of the file
    while lines_to_go > 0 and block_end_byte > 0:
        if (block_end_byte - BLOCK_SIZE > 0):
            # read the last block we haven't yet read
            f.seek(block_number * BLOCK_SIZE, 2)
            blocks.append(f.read(BLOCK_SIZE))
        else:
            # file too small, start from begining
            f.seek(0, 0)
            # only read what was not read
            blocks.append(f.read(block_end_byte))
        lines_found = blocks[-1].count('\n')
        lines_to_go -= lines_found
        block_end_byte -= BLOCK_SIZE
        block_number -= 1
    all_read_text = ''.join(reversed(blocks))
    return all_read_text.splitlines()[-total_lines_wanted:]



def getCompletedNodes(filename):
    lines = tail2(open(filename), 30)
    # print "got lines, size:", len(lines)
    node = 0
    for line in lines:
        # print line
        if "NODE_" in line:
            node = re.findall('NODE_[0-9]+', line)[0]
        if "Total run Time:" in line:
            return (node, filename)
    return (False, False)

def getShardId():
    f = open('sourcerer-cc.properties')
    for line in f:
        if "SEARCH_SHARD_ID" in line:
            pair = line.split("=")
            shard_id = pair[1].strip()
            f.close()
            return shard_id
            

def getJobName():
    f = open ('worker.sh')
    for line in f:
        if "-N" in line:
            job = line.split()[-1]
            f.close()
            return job.strip()
    pass

def getCompletedQueries():
    #logFiles = getLogFiles()
    nodes = getAllNodeFolders()
    queries = []
    print "Total {0} nodes found".format(len(nodes))
    count = 0
    for node in nodes:
        count += 1
        print "processing {0} of {1}: node: {2}".format(count, len(nodes), node)
        try:
            filename = "{0}/output8.0/queryclones_index_WITH_FILTER.txt".format(node)
            f = open(filename, 'r')
            previous = ""
            added = False
            for line in f:
                try:
                    #if "TOTAL candidates" in line:
                        #query = line.split(" ")[5]
                        #query = query.split(",")[1]
                    parts = line.split(",")
                    query = parts[1]
                    if query != previous:
                        added = True
                        queries.append(query)
                        previous = query
                except:
                    pass  # ignroe
            # ignore the last query as it might not be complete
            if added:
                queries = queries[:-1]
            f.close()
        except:
            print("no output file in ", node)
            pass  # ignore
    # ignore the last query as it might not be complete
    return queries

def getLogFiles():
    print "getting log files"
    FILE_NAME = getJobName()
    filesToReturn = []
    for root, subFolders, files in os.walk('./'):
        for f in files:
            fileName, fileExtension = os.path.splitext(f)
            if fileExtension.startswith('.o'):
                if fileName == FILE_NAME:
                    try:
                        print "adding {0}".format(f)
                        filesToReturn.append(f)
                    except:
                        print "sys.exc_info:", sys.exc_info()[0]
        break
    return filesToReturn

def getLogFilesNonHpc():
    print "getting log files"
    filesToReturn = []
    for root, subFolders, files in os.walk('./'):
        for f in files:
            fileName, fileExtension = os.path.splitext(f)
            if fileExtension.startswith('.log'):
                try:
                    print "adding {0}".format(f)
                    filesToReturn.append(f)
                except:
                    print "sys.exc_info:", sys.exc_info()[0]
        break
    return filesToReturn

def createWorkers(start_worker_id=1, end_worker_id=256, queue_name="free64", action="search"):
    import os
    import stat
    #$ -ckpt blcr
    worker_template = """#!/bin/bash
#$ -N {task}
#$ -q {queue_name}
#$ -pe openmp 2-3
node={node_id}
rootPATH=`pwd`
threshold=8
#ant clean cdi
echo "running on $node"
java -Dproperties.location="$rootPATH/NODE_$node/sourcerer-cc.properties" -Xms6g -Xmx6g  -jar dist/indexbased.SearchManager.jar {task} $threshold
echo "done"
"""
    
    for i in range(start_worker_id,end_worker_id+1):
        text = worker_template.format(queue_name=queue_name,node_id=i,task=action)
        filename="worker_{id}.sh".format(id=i)
        f = open(filename,'w')
        f.write(text)
        f.close()
        st = os.stat(filename)
        os.chmod(filename, st.st_mode | stat.S_IEXEC)



def getAllNodeFolders():
    nodes = []
    for root, subFolders, files in os.walk('./'):
            # print "root", root
            for subfolder in subFolders:
                if subfolder.startswith('NODE_'):
                    nodes.append(subfolder)
            break
    return nodes

def transferAllData():
    nodes = getAllNodeFolders()
    node_file = open("nodes.txt", 'w')
    for node in nodes:
        node_file.write(node+"\n")
    node_file.close()
#     finished_nodes = open("finishedNodes.txt", 'w')
#     finished_logs = open("finishedLogs.txt", 'w')
#     for root, subFolders, files in os.walk('./'):
#         # print "root", root
#         for f in files:
#             fileName, fileExtension = os.path.splitext(f)
#             if fileExtension.startswith('.o'):
#                 if fileName == FILE_NAME:
#                     try:
#                         # print "processing:", os.getcwd() , f
#                         node, log = getCompletedNodes(os.getcwd() + "/" + f)
#                         if node and log:
#                             finished_nodes.write(node + '\n')
#                             finished_logs.write(f + '\n')
#                     except:
#                         print "sys.exc_info:", sys.exc_info()[0]
#         break
#     finished_nodes.close()
#     finished_logs.close()
if __name__ == '__main__':
    
    # Hello World program in Python
    import os
    import re
    import sys
    
    if "lc" == sys.argv[1]:
       transferAllData()
    elif "job" == sys.argv[1]:
        print(getShardId())
    elif "rr" == sys.argv[1]:
        f = open("completed_queries.txt", 'a')
        queries = getCompletedQueries()
        print "{0} queries found".format(len(queries))
        for query in queries:
            f.write(query + "\n")
        f.close()
    elif "gw" == sys.argv[1]:
        start_worker_id = int(sys.argv[2])
        end_worker_id = int(sys.argv[3])
        action = sys.argv[4]
        print "creating workers ", start_worker_id, end_worker_id, action
        queue_name="free64,pub64,free48,pub8i,free40i,free32i,free24i"
        createWorkers(start_worker_id=start_worker_id,end_worker_id=end_worker_id,queue_name=queue_name,action=action)
        
