from xml.dom import minidom
nicad_clones_file_path='/lv_scratch/scratch/mondego/local/farima/new_oreo/toolsEval/NiCad-4.0/nicad4results/bcb_reduced_functions-clones/bcb_reduced_functions-clones-0.30.xml'
# nicad_clones_file_path='D:\\PhD\\Clone\\nicad_results.txt'
clonepairs_output_path='/lv_scratch/scratch/mondego/local/farima/new_oreo/toolsEval/NiCad-4.0/nicad4results/nicad0.3_bce_formatted.txt'
# clonepairs_output_path='..\\nicad0.3_bce_formatted.txt'
file_output=open(clonepairs_output_path,'w')
# parse an xml file by name
mydoc = minidom.parse(nicad_clones_file_path)

items = mydoc.getElementsByTagName('clone')


for elem in items:
    methods=elem.getElementsByTagName('source')
    line_write=''
    for method in methods:
        filepath=method.attributes['file'].value
        filepath_splitted=filepath.split('/')
        directory=filepath_splitted[3]
        file=filepath_splitted[4]
        startline=method.attributes["startline"].value
        endline = method.attributes["endline"].value
        line_write+=directory+','+file+','+startline+','+endline+','
    file_output.write(line_write[:len(line_write)-1]+'\n')
file_output.close()