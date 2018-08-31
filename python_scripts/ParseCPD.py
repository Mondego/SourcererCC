from xml.dom import minidom
cpd_clones_file_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/pmd-bin-6.6.0/pcd_clones.xml'
# cpd_clones_file_path='D:\\pcd_clones.xml'

cpd_clones_file_withoutsource_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/pmd-bin-6.6.0/pcd_clones_withoutsource.xml'
# cpd_clones_file_withoutsource_path='D:\\pcd_clones_withoutsource.xml'

clonepairs_output_path='/lv_scratch/scratch/mondego/local/farima/IC-tools/pmd-bin-6.6.0/pcd_clonepairs_bceformatted.txt'
# clonepairs_output_path='D:\\pcd_clonepairs_bceformatted.txt'

file_withoutsource_output=open(cpd_clones_file_withoutsource_path,'w')
ignore=False
with open(cpd_clones_file_path,'r') as file_cpd:
    for line in file_cpd:
        if ignore and line.strip().__contains__('</codefragment>'):
            ignore=False
            continue
        if line.strip().startswith('<codefragment>'):
            ignore=True
            continue
        elif not ignore:
            file_withoutsource_output.write(line)

file_withoutsource_output.close()
file_output=open(clonepairs_output_path,'w')

# parse an xml file by name
mydoc = minidom.parse(cpd_clones_file_withoutsource_path)

items = mydoc.getElementsByTagName('duplication')
print('num of duplications: '+str(len(items)))
dup_count=0
for elem in items:
    dup_count+=1
    print('duplication number: '+ str(dup_count))

    num_lines =elem.attributes['lines'].value
    files=elem.getElementsByTagName('file')
    line_write=''
    for i in range(0, len(files)):
        filepath1 = files[i].attributes['path'].value
        filepath1_splitted = filepath1.split('/')
        directory1 = filepath1_splitted[len(filepath1_splitted) - 2]
        file1 = filepath1_splitted[len(filepath1_splitted) - 1]
        startline1 = files[i].attributes['line'].value
        endline1 = (int(startline1) + int(num_lines)) - 1
        for j in range(i + 1, len(files)):
            filepath2 = files[j].attributes['path'].value
            filepath2_splitted = filepath2.split('/')
            directory2 = filepath2_splitted[len(filepath2_splitted) - 2]
            file2 = filepath2_splitted[len(filepath2_splitted) - 1]
            startline2 = files[j].attributes['line'].value
            endline2 = (int(startline2) + int(num_lines))-1
            line_write=directory1+','+file1+','+startline1+','+str(endline1)+','+directory2+','+file2+','+startline2+','+str(endline2)
            file_output.write(line_write+'\n')
file_output.close()