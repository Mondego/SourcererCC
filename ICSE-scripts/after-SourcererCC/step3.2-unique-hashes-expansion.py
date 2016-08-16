# This is one of two parts to obtain the final list of file clones.
# In this step the total list of 100% (hash-equal) file clone pairs
#    is calculated

#Usage:
# $ python step3.2-unique-hashes-expansion.py [tokenizer-result]/tokens > final-list-100-clone-pairs.txt

import os
import sys
import itertools

path_to_tokens_folder = file_id_1 = sys.argv[1]

file_id_md5_hash = dict()

for file in os.listdir(path_to_tokens_folder):
        if file.endswith('.txt'):
                file = os.path.join(path_to_tokens_folder,file)
                #print 'Searching on ',file
                with open(file,'r') as file_io:
                        for line in file_io:
                                line_split = (line.split('@#@'))[0].split(',')
                                file_id = line_split[0]+','+line_split[1]
                                file_hash = line_split[4]
                                if file_hash in file_id_md5_hash:
                                        file_id_md5_hash[file_hash].append(file_id)
                                else:
                                        file_id_md5_hash[file_hash] = [file_id]


for key in file_id_md5_hash:
        for (a,b) in itertools.combinations(file_id_md5_hash[key],2):
                print a+','+b


