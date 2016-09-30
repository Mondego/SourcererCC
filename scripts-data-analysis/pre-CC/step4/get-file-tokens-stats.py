#Gets some stats about the list of token-distinct files
#Usage: $python get-file-tokens-stats.py ../step3/distinct-file-hashes/distinct-files-tokens.tokens distinct-token-hashes/distinct-files-tokens.tokens

import sys
import os

dict_hashes = dict()

with open(sys.argv[1],'r') as file:
  for line in file:
    file_hash = (line.split('@#@')[0]).split(',')[4]

    if file_hash in dict_hashes:
      dict_hashes[file_hash] = dict_hashes[file_hash] + 1
    else:
      dict_hashes[file_hash] = 1

sum_total_tokens = 0
sum_tokens_maior_65 = 0
files_maior_65 = 0
total_number_unique_files = 0
total_number_unique_files_maior_65 = 0

with open(sys.argv[2],'r') as file:
  for line in file:
    total_tokens = int(line.split(',')[2])
    sum_total_tokens += total_tokens

    file_hash = (line.split('@#@')[0]).split(',')[4]

    if dict_hashes[file_hash] == 1:
      total_number_unique_files += 1

    if total_tokens >= 65:
      files_maior_65 += 1
      sum_tokens_maior_65 += total_tokens
      if dict_hashes[file_hash] == 1:
        total_number_unique_files_maior_65 += 1

print '***** Tokens stats *****'
print 'Absolute number of tokens:      ',sum_total_tokens
print 'Number of tokens for files 65+: ',sum_tokens_maior_65
print '***** Files  stats *****'
print 'Number of token-distinct files :',len(dict_hashes)
print '    from which these are unique:',total_number_unique_files
print 'Number of files with 65+ tokens:',files_maior_65
print '    from which these are unique:',total_number_unique_files_maior_65


