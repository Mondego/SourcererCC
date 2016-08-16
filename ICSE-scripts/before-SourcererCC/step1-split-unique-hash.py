# Script that takes a list of tokens and creates a list of files representing
#   unique tokens based on their md5 hash. In practice, this removes file
#   clones at 100%
# The input of this script will be the concatenation of all files under the
#   folder 'tokens\', as produced by the tokenizer
# The result of this script will be the input to SourcererCC

# Usage:
# $ python step1-split-unique-hash.py > unique-hashes.txt
import sys

file = sys.argv[1]

res = dict()

with open(file,'r') as file:
        for line in file:
                hash_value = (line.split('@#@')[0]).split(',')[4]
                if hash_value not in res:
                        res[hash_value] = line[:-1]

for k in res.keys():
        print res[k]

