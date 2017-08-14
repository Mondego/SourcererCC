import os.path
file_output=open('output/IjaMapping_methodchars.txt', 'w')

with open('input/IjaMapping_new_uniquetokens.txt','r') as  file_input:
    for line in file_input:
        line_splitted=line.replace('\n','').replace('\r','').split(':')
        print(line_splitted)
        file_info=line_splitted[1].split(',')
        dir_name=file_info[0]
        file_name = file_info[1]
        startline = int(file_info[2])
        endline = int(file_info[3])
        i=0
        uniquecharsset=set()
        uniquechars = ''
        if os.path.isfile(dir_name+'/'+file_name):
            with open(dir_name+'/'+file_name) as file:
                for line_code in file:
                    i+=1
                    if i==int(startline):
                        uniquecharsset=uniquecharsset|set(line_code)
                    elif i>startline and i<endline:
                        uniquecharsset = uniquecharsset | set(line_code)
                        #print(line_code)
                    elif i==endline:
                        uniquecharsset = uniquecharsset | set(line_code)
                    elif i > endline:
                        break
            uniquechars=''.join(uniquecharsset)
            file_output.write(line_splitted[0]+':'+line_splitted[1]+','+uniquechars.replace('\n','').replace('\r','').replace(',','').replace('~','')+'\n')
            file.close()
file_output.close()