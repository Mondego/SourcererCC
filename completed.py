'''
Created on Apr 11, 2016

@author: saini
'''

def tail(f, n, offset=0):
    #print "tailing", f, n
    stdin,stdout = os.popen2("tail -n "+n+offset+" "+f)
    stdin.close()
    lines = stdout.readlines(); stdout.close()
    return lines[:,-offset]


def tail2( f, lines=10 ):
    #print ("tailing ")
    total_lines_wanted = lines

    BLOCK_SIZE = 1024
    f.seek(0, 2)
    block_end_byte = f.tell()
    lines_to_go = total_lines_wanted
    block_number = -1
    blocks = [] # blocks of size BLOCK_SIZE, in reverse order starting
                # from the end of the file
    while lines_to_go > 0 and block_end_byte > 0:
        if (block_end_byte - BLOCK_SIZE > 0):
            # read the last block we haven't yet read
            f.seek(block_number*BLOCK_SIZE, 2)
            blocks.append(f.read(BLOCK_SIZE))
        else:
            # file too small, start from begining
            f.seek(0,0)
            # only read what was not read
            blocks.append(f.read(block_end_byte))
        lines_found = blocks[-1].count('\n')
        lines_to_go -= lines_found
        block_end_byte -= BLOCK_SIZE
        block_number -= 1
    all_read_text = ''.join(reversed(blocks))
    return all_read_text.splitlines()[-total_lines_wanted:]



def getCompletedNodes(filename):
    lines = tail2(open(filename),30)
    #print "got lines, size:", len(lines)
    node=0
    for line in lines:
        #print line
        if "NODE_" in line:
            node = re.findall('NODE_[0-9]+',line)[0]
        if "Total run Time:" in line:
            return (node, filename)
    return (False, False)

def getShardId():
    f = open('sourcerer-cc.properties')
    for line in f:
        if "SEARCH_SHARD_ID" in line:
            pair= line.split("=")
            shard_id =pair[1].strip()
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
    nodes = getAllNodeFolders()
    queries=[]
    for node in nodes:
        filename = './{0}/{1}/{2}'.format(node,'output8.0','queryclones_index_WITH_FILTER.txt')
        try:
            f = open(filename,'r')
            previous=""
            added=False
            for line in f:
                try:
                    
                    query = line.split(",")[0]
                    if query != previous:
                        added=True
                        queries.append(query)
                        previous = query
                except:
                    pass #ignroe
            # ignore the last query as it might not be complete
            if added:
                queries = queries[:-1]
            f.close()
        except:
            print("no output file in ", node)
            pass # ignore
    # ignore the last query as it might not be complete
    return queries

def getAllNodeFolders():
    nodes=[]
    for root, subFolders, files in os.walk('./'):
            #print "root", root
            for subfolder in subFolders:
                if subfolder.startswith('NODE_'):
                    nodes.append(subfolder)
            break
    return nodes
if __name__ == '__main__':
    
    # Hello World program in Python
    import os
    import re
    import sys
    
    if "lc" == sys.argv[1]:
        FILE_NAME = getJobName()
        finished_nodes = open("finishedNodes.txt",'w')
        finished_logs = open("finishedLogs.txt",'w')
        for root, subFolders, files in os.walk('./'):
            #print "root", root
            for f in files:
                fileName, fileExtension = os.path.splitext(f)
                if fileExtension.startswith('.o'):
                    if fileName == FILE_NAME:
                        try:
                            #print "processing:", os.getcwd() , f
                            node, log =getCompletedNodes(os.getcwd()+"/"+f)
                            if node and log:
                                finished_nodes.write(node+'\n')
                                finished_logs.write(f+'\n')
                        except:
                            print "sys.exc_info:", sys.exc_info()[0]
            break
        finished_nodes.close()
        finished_logs.close()
    elif "job" == sys.argv[1]:
        print(getShardId())
    elif "rr" == sys.argv[1]:
        f = open("completed_queries.txt",'a')
        queries = getCompletedQueries()
        print "{} queries found".format(len(queries))
        for query in queries:
            f.write(query+"\n")
        f.close()
