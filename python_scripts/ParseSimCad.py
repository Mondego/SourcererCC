simcad_clones_file_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/recall_dataset_simcad_clones/simcad_function_clone-pairs_type-1-2-3_greedy.xml'
# simcad_clones_file_path='D:\\simcad.txt'
clonepairs_output_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/recall_dataset_simcad_clones/simcad_pairs_bceformatted.txt'
# clonepairs_output_path='D:\\simcad_bce_formatted.txt'
file_output=open(clonepairs_output_path,'w')

def parseLine(line):
    print(line)
    linesplitted=line.split(' ')
    path=linesplitted[len(linesplitted)-4]
    pathsplitted=path.split('/')
    folder=pathsplitted[len(pathsplitted)-2]
    file=pathsplitted[len(pathsplitted)-1].rstrip('"')
    startline=linesplitted[len(linesplitted)-3].lstrip('startline="').rstrip('"')
    endline = linesplitted[len(linesplitted) - 2].lstrip('endline="').rstrip('"')
    return folder+','+file+','+startline+','+endline

numpairs=0
pair_detected=False
cp1=''
cp2=''
with open(simcad_clones_file_path,'r') as file_simcad:
    for line in file_simcad:
        if line.startswith('<ClonePair'):
            numpairs+=1
            pair_detected=True
            continue
        elif line.startswith('<CloneFragment') and pair_detected:
            cp1=parseLine(line)
            pair_detected=False
            continue
        elif line.startswith('<CloneFragment file=') and not pair_detected:
            cp2=parseLine(line)
            continue
        elif line.startswith('</ClonePair>'):
            file_output.write(cp1+ ',' + cp2+'\n')
print('number of pairs: '+str(numpairs))
file_output.close()
