javakeywords=set({'abstract','assert','boolean','break','byte','case','catch','char',
                  'class','continue','default','do','double','else','enum','extends',
                  'final','finally','float','for','if','implements','import','instanceof',
                  'int','interface','long','native','new','package','private','protected',
                  'public','return','short','static','super','switch','synchronized','this',
                  'throw','throws','transient','try','void','volatile','while','true','false','null'})#true, false, null are literals but they maybe valuable in detecting clones
javakeywords.add('abstract')

file_output=open('output/NoTokenMethods_Features.txt', 'w')

# dict_methodfeatures=dict()
# with open('output/.txt', 'r') as file_features:
#     for line in file_features:
#         line_splitted=line.replace('\n','').replace('\r','').split('@#@')
#         meta=line_splitted[0]
#         props=meta.split('~~')
#         projid=props[0]
#         fileid=props[1]
#         dict_methodfeatures[projid+','+fileid]=props[2]+','+props[3]

dictmethods=dict()
with open('output/MethodNoToken.txt','r') as file_input:
    for line in file_input:
        line_splitted=line.replace('\n','').replace('\r','').split(':')
        fqmn=line_splitted[0]
        info=line_splitted[1].split(',')
        line_count=(int(info[3])-int(info[2])+1)
        numtokens=info[4]
        numuniquetokens=info[5]
        #fqmn@#@linecount,numtoken,numuniquetokens
        linetowrite=fqmn+'@#@'+str(line_count)+','+numtokens+','+numuniquetokens
        file_output.write(linetowrite+'\n')
file_output.close()