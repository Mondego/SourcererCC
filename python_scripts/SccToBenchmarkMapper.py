import os

file_methodid={}
scc_clonepairs_path='/scratch/mondego/local/farima/new_oreo/train_related/SourcererCC/clone-detector/scc_clonepairs_6_15_tokens_and_more.txt'
output_dir='/scratch/mondego/local/farima/new_oreo/scc_related'

if (not os.path.isdir(output_dir)):
    os.makedirs(output_dir)

file_output = open(output_dir+'/scc_benchmark_formatted_6th_15tok.txt', 'w')

with open('/scratch/mondego/local/farima/new_oreo/train_related/train_input_generation_code/SourcererCC/python_scripts/scc_bookkeeping.file') as file_bookkeeping:
    for line in file_bookkeeping:
        linesplitted=line.split(";")
        filepart=linesplitted[0].split(":")
        methodpart = linesplitted[1].split(":")
        filefullpath=filepart[1].split("/")
        file_dir=filefullpath[-2]
        filename=filefullpath[-1]
        line_nums=linesplitted[2].split(':')
        if(line_nums[1][-1:]=='\n'): line_nums[1]=line_nums[1][:-1]
        file_methodid[filepart[0]+','+methodpart[0]]=file_dir+','+filename+','+line_nums[0]+','+line_nums[1]
        print(file_dir+','+filename+','+line_nums[0]+','+line_nums[1])

with open(scc_clonepairs_path) as file_scc_cps:
    for line in file_scc_cps:
        linesplitted=line.split(',')
        cp1=file_methodid[linesplitted[0]+','+linesplitted[1]]
        if(linesplitted[3][-1:]=='\n'): linesplitted[3]=linesplitted[3][:-1]
        cp2 = file_methodid[linesplitted[2] + ',' + linesplitted[3]].rstrip('\n')
        file_output.write(cp1+','+cp2+'\n')
file_output.close()
