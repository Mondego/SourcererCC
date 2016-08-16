# This is one of two parts to obtain the final list of file clones.
# In this step the results from SourcererCC are expanded with the
#   total list of file clones

#Usage:
# $ python step3.1-CC-hash-expansion.py  [tokenizer-result]/tokens SourcererCC-hash-results.txt > final-list-clone-pairs.txt

import os
import sys

path_to_tokens_folder = file_id_1 = sys.argv[1]
path_to_CC_hash_pairs = sys.argv[2]

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


with open(path_to_CC_hash_pairs,'r') as CC_output:
        for line in CC_output:
                line_split = line[:-1].split(',')
                file1 = line_split[0]
                file2 = line_split[1]
                list_files1 = file_id_md5_hash[file1]
                list_files2 = file_id_md5_hash[file2]
                for p1 in list_files1:
                        for p2 in list_files2:
                                print p1+','+p2

