# Creates a group of files whose file-hash is representative of a group
# Usage $pythoon this-script.py files_stats/ files_tokens/

import sys
import os
import csv

PATH_distinct_file_hashes = 'distinct-file-hashes'

if os.path.exists(PATH_distinct_file_hashes):
  print 'ERROR - Folder ['+PATH_distinct_file_hashes+'] already exists!'
  sys.exit()
else:
  os.makedirs(PATH_distinct_file_hashes)

set_ids    = set()
set_hashes = set()

for file in os.listdir(sys.argv[1]):
  if file.endswith('.stats'):
    file = os.path.join(sys.argv[1],file)
    print 'Searching on ',file
    with open(file, 'r') as csv_file_book, open(os.path.join(PATH_distinct_file_hashes,'distinct-files.stats'),'a+') as result_stats:
      csv_reader = csv.reader(csv_file_book, delimiter=',')
      for entry in csv_reader:
        file_hash = entry[4]
        if file_hash not in set_hashes:
          result_stats.write(','.join(entry)+'\n')
          set_hashes.add(file_hash)
          set_ids.add(entry[1])

for file in os.listdir(sys.argv[2]):
  if file.endswith('.tokens'):
    file = os.path.join(sys.argv[2],file)
    print 'Searching on ',file
    with open(file,'r') as file_book, open(os.path.join(PATH_distinct_file_hashes,'distinct-files.tokens'),'a+') as result_tokens:
      for line in file_book:
        file_id = line.split(',')[1]
        if file_id in set_ids:
          result_tokens.write(line)


