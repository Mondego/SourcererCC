# This step converts the list of file pairs from SourcererCC into
#   a list of file hashes. THis will be useful as this list of 
#   file hashes will be used to expand hash-equal results into
#   total list of results

# Usage:
# $ python step2-file_pairs-to-hash_pairs.py [tokenizer-result]/tokens SourcererCC-results-norepetition.txt > SourcererCC-hash-results.txt

import os
import sys

path_to_tokens_folder = file_id_1 = sys.argv[1]
path_to_SourcererCC_output = sys.argv[2]

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
                                file_id_md5_hash[file_id] = file_hash

with open(path_to_SourcererCC_output,'r') as CC_output:
        for line in CC_output:
                line_split = line[:-1].split(',')
                file1 = line_split[0]+','+line_split[1]
                file2 = line_split[2]+','+line_split[3]
                print (file_id_md5_hash[file1]+','+file_id_md5_hash[file2])

