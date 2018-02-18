cloneworks_clones_file_path='/lv_scratch/scratch/mondego/local/farima/new_oreo/toolsEval/CloneWorks-0.3/bcb_reduced.clones.csv'
# nicad_clones_file_path='D:\\PhD\\Clone\\nicad_results.txt'
clonepairs_output_path='/lv_scratch/scratch/mondego/local/farima/new_oreo/toolsEval/CloneWorks-0.3/cloneworks_clonepairs_bceformatted.txt'
# clonepairs_output_path='..\\nicad0.3_bce_formatted.txt'
file_output=open(clonepairs_output_path,'w')

with open(cloneworks_clones_file_path,'r') as file_cloneworks:
    for line in file_cloneworks:
        linesplitted=line.split(',')
        startline1=linesplitted[1]
        endline1=linesplitted[2]
        startline2=linesplitted[4]
        endline2=linesplitted[5]
        path1splitted=linesplitted[0].split('/')
        path1=path1splitted[len(path1splitted)-2]+','+path1splitted[len(path1splitted)-1]
        path2splitted = linesplitted[3].split('/')
        path2 = path2splitted[len(path2splitted) - 2] + ',' + path2splitted[len(path2splitted) - 1]
        file_output.write(path1+','+startline1+','+endline1+','+path2+','+startline2+','+endline2+'\n')
file_output.close()