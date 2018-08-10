import re

iclones_clones_file_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/iclones-0.2/iclones_bcb.txt'
# iclones_clones_file_path='D:\\iclones_test.txt'
clonepairs_output_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/iclones-0.2/iclones_bcb_bceformatted.txt'
# clonepairs_output_path='D:\\iclones_bce_formatted.txt'
file_output=open(clonepairs_output_path,'w')

def parseLine(line):
    linesplitted = line.split(' ')
    if len(linesplitted)>=5:
        path=linesplitted[2].split('/')
        folder=path[len(path)-2]
        file=path[len(path)-1]
        return folder+','+file+','+linesplitted[len(linesplitted)-2]+','+linesplitted[len(linesplitted)-1]

def getClonepairsfromCloneclass(clone_class):
    print(len(clone_class))
    clone_class = list(clone_class)
    for i in range(0, len(clone_class)):
        for j in range(i + 1, len(clone_class)):
            file_output.write(clone_class[i] + ',' + clone_class[j] + '\n')


with open(iclones_clones_file_path,'r') as file_iclones:
    clone_class=[]
    clone_class_found = False
    for line in file_iclones:
        if line.__contains__('CloneClass'):
            getClonepairsfromCloneclass(clone_class)
            print("one clone class found")
            clone_class_found=True
            clone_class=[]
            continue
        elif clone_class_found or len(clone_class)>0:
            clone_class_found=False
            parsed_line=parseLine(re.sub(' +',' ',line.replace("\n","")))
            if parsed_line!=None:
                clone_class.append(parsed_line)
print(clone_class)
getClonepairsfromCloneclass(clone_class) #parse the last class
        # linesplitted=line.split(',')
        # startline1=linesplitted[1]
        # endline1=linesplitted[2]
        # startline2=linesplitted[4]
        # endline2=linesplitted[5]
        # path1splitted=linesplitted[0].split('/')
        # path1=path1splitted[len(path1splitted)-2]+','+path1splitted[len(path1splitted)-1]
        # path2splitted = linesplitted[3].split('/')
        # path2 = path2splitted[len(path2splitted) - 2] + ',' + path2splitted[len(path2splitted) - 1]
        # file_output.write(path1+','+startline1+','+endline1+','+path2+','+startline2+','+endline2+'\n')
file_output.close()