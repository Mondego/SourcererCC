# Script that takes a list of tokens
# and creates a list of files representing
# unique tokens based on their md5 hash

# Usage:
# $ python split-unique-hash.py > unique-hashes.txt

file = 'tokens-file-path'

res = dict()

with open(file,'r') as file:
        for line in file:
                hash_value = (line.split('@#@')[0]).split(',')[4]
                if hash_value not in res:
                        res[hash_value] = line[:-1]

for k in res.keys():
        print res[k]
