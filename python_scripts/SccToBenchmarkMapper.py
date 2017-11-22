import os

file_methodid={}
scc_clonepairs_path='/scratch/mondego/local/farima/new_oreo/recall_related/scc/SourcererCC/clone-detector/clonepairs.txt'
output_dir='/scratch/mondego/local/farima/new_oreo/scc_related'

if (not os.path.isdir(output_dir)):
    os.makedirs(output_dir)
file_output = open(output_dir+'/scc_benchmark_formatted.txt', 'w')

with open('/scratch/mondego/local/farima/new_oreo/recall_related/metricCalculator/SourcererCC/python_scripts/1_metric_output/bookkeeping.file') as file_bookkeeping:
    for line in file_bookkeeping:
        linesplitted=line.split(";")
        filepart=linesplitted[0].split(":")
        methodpart = linesplitted[1].split(":")
        filefullpath=filepart[1].split("/")
        file_dir=filefullpath[-2]
        filename=filefullpath[-1]
        line_nums=linesplitted[2].split(':')
        file_methodid[filepart[0]+','+methodpart[0]]=file_dir+','+filename+','+line_nums[0]+','+line_nums[1]

with open(scc_clonepairs_path) as file_scc_cps:
    for line in file_scc_cps:
        linesplitted=line.split(',')
        cp1=file_methodid[linesplitted[0]+','+linesplitted[1]]
        cp2 = file_methodid[linesplitted[2] + ',' + linesplitted[3]]
        file_output.write(cp1+','+cp2+'\n')
file_output.close()