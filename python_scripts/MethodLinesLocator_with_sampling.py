import random

file_output=open('/lv_scratch/scratch/mondego/local/farima/tensorFlow/experiments/NNver2_400samples_50TokensOrMore.txt','w')

with open('/scratch/mondego/local/farima/new_oreo/sampling_for_precision_related/output_sampled_clone_pairs_file/sampled_NN_ver2.txt','r') as file_samples:
    for line in file_samples:
        line_splitted=line.replace('\n','').split(',')
        i=0
        with open('/lv_scratch/scratch/mondego/local/farima/new_oreo/recall_related/recall_dataset/'+line_splitted[0]+'/'+line_splitted[1]) as file1:
            for line_code in file1:
                i+=1
                if i==int(line_splitted[2]):
                    print(line_code)
                    print(i)
                    file_output.write('$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$'+'\n')
                    file_output.write(line_splitted[0]+','+line_splitted[1]+'\n')
                    file_output.write(line_splitted[4] + ',' + line_splitted[5] + '\n')
                    file_output.write('----------------------------------------' + '\n')
                    file_output.write(line_code)
                elif i>int(line_splitted[2]) and i<int(line_splitted[3]):
                    file_output.write(line_code)
                    #print(line_code)
                elif i==int(line_splitted[3]):
                    print(line_code)
                    file_output.write(line_code)
                    file_output.write('----------------------------------------'+'\n')
                elif i > int(line_splitted[3]):
                    break
        file1.close()
        i=0
        line_code=''
        with open('/lv_scratch/scratch/mondego/local/farima/new_oreo/recall_related/recall_dataset/'+line_splitted[4]+'/'+line_splitted[5]) as file2:
            for line_code in file2:
                i+=1
                if i == int(line_splitted[6]):

                    file_output.write(line_code)
                elif i > int(line_splitted[6]) and i < int(line_splitted[7]):
                    file_output.write(line_code)
                elif i == int(line_splitted[7]):
                    file_output.write(line_code)
                    file_output.write('----------------------------------------' + '\n')
                elif i > int(line_splitted[7]):
                    break
        file2.close()
file_output.close()
