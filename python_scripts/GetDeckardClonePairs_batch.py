import os.path
from os import listdir
from os.path import isfile, join


file_error_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/deckard/Deckard-parallel1.3/deckard_parsing_error.txt'
deckard_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/deckard/Deckard-parallel1.3/clusters'
# file_error_path='deckard_parsing_error.txt'
# file_deckard_path='D:\\PhD\\Clone\\deckard_clusters.txt'
# file_output_path='deckard_clonepairs.txt'

file_error=open(file_error_path,'w')

def parseline(line):
    linesplitted=line.split(' ')
    path=linesplitted[1].split('/')
    path_to_write=path[11]+','+path[12]
    startline=linesplitted[2].split(':')[1]
    endline=int(startline)+int(linesplitted[2].split(':')[2])-1
    return (path_to_write+','+startline+','+str(endline))


def getclonepairs(cluster,i):
    print(len(cluster))
    file_output = open(deckard_path+str(i)+'/deckard_bceformatted.txt', 'w')
    cluster_list=list(cluster)
    for i in range(0,len(cluster_list)):
        for j in range(i+1,len(cluster_list)):
            file_output.write(cluster_list[i]+','+cluster_list[j]+'\n')
    file_output.close()

for i in range(2,46):
    file_input = [f for f in listdir(deckard_path+str(i)+'/') if isfile(join(deckard_path+str(i)+'/', f))]
    print(file_input+' processing')
    with open(deckard_path+str(i)+'/'+file_input,'r') as file_deckard:
        future_cluster_num=0
        current_cluster_num=0
        cluster_set=set()
        linenum=0
        for line in file_deckard:
            linenum+=1
            try:
                if linenum>9:
                    if (line  in ['\n', '\r\n']):
                        print('one cluster found')
                        future_cluster_num+=1
                        continue
                    if (future_cluster_num>current_cluster_num):
                        if (len(cluster_set)>0): getclonepairs(cluster_set,i)
                        cluster_set = set()
                        current_cluster_num=future_cluster_num
                    cluster_set.add(parseline(line))
            except Exception as e:
                print(str(e))
                file_error.write('file '+str(i)+' : error at line ' + str(linenum)+'\n')

        try:
            getclonepairs(cluster_set,i)
        except:
            file_error.write('file '+str(i)+' : error at line ' + str(linenum)+'\n')

file_error.close()

