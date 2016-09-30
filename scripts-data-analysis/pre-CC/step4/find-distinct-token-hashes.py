# Creates a group of files whose token-hash is representative of a group
# Usage $pythoon this-script.py distinct-token-hash-tokens-list.tokens distinct-file-hash-stats-list.stats

import sys
import os

PATH_distinct_token_hashes = 'distinct-token-hashes'

if os.path.exists(PATH_distinct_token_hashes):
  print 'ERROR - Folder ['+PATH_distinct_token_hashes+'] already exists!'
  sys.exit()
else:
  os.makedirs(PATH_distinct_token_hashes)

set_ids    = set()
set_hashes = set()

print 'Searching on ',sys.argv[1]
with open(sys.argv[1],'r') as file_book, open(os.path.join(PATH_distinct_token_hashes,'distinct-tokens.tokens'),'a+') as result_tokens:
  for line in file_book:
    file_hash = (line.split('@#@')[0]).split(',')[4]
    if file_hash not in set_hashes:
      result_tokens.write(line)
      set_hashes.add(file_hash)
      set_ids.add(line.split(',')[1])

print 'Searching on ',sys.argv[1]
with open(sys.argv[2],'r') as file_book, open(os.path.join(PATH_distinct_token_hashes,'distinct-tokens.stats'),'a+') as result_stats:
  for line in file_book:
    file_id = line.split(',')[1]
    if file_id in set_ids:
      result_stats.write(line)
