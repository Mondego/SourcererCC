# Creates a group of files whose file-hash is unique
# Usage $pythoon this-script.py files_stats/ files_tokens/

import sys
import os
import csv

PATH_unique_file_hashes = 'unique-file-hashes'

if os.path.exists(PATH_unique_file_hashes):
  print 'ERROR - Folder ['+PATH_unique_file_hashes+'] already exists!'
  sys.exit()
else:
  os.makedirs(PATH_unique_file_hashes)

set_ids    = set()
dict_hashes_frequency = {}

for file in os.listdir(sys.argv[1]):
  if file.endswith('.stats'):
    file = os.path.join(sys.argv[1],file)
    print 'Searching on ',file
    with open(file,'r') as file_book:
      csv_reader = csv.reader(file_book, delimiter=',')
      for entry in csv_reader:
        file_hash = entry[4]
        if file_hash not in dict_hashes_frequency:
          dict_hashes_frequency[file_hash] = 1
        else:
          dict_hashes_frequency[file_hash] += 1

for file in os.listdir(sys.argv[1]):
  if file.endswith('.stats'):
    file = os.path.join(sys.argv[1],file)
    print 'Searching on ',file
    with open(file,'r') as file_book, open(os.path.join(PATH_unique_file_hashes,'unique-files.stats'),'a+') as result_stats:
      csv_reader = csv.reader(file_book, delimiter=',')
      for entry in csv_reader:
        file_hash = entry[4]
        if dict_hashes_frequency[file_hash] == 1:
          result_stats.write(','.join(entry)+'\n')
          set_ids.add(entry[1])

for file in os.listdir(sys.argv[2]):
  if file.endswith('.tokens'):
    file = os.path.join(sys.argv[2],file)
    print 'Searching on ',file
    with open(file,'r') as file_book, open(os.path.join(PATH_unique_file_hashes,'unique-files.tokens'),'a+') as result_tokens:
      for line in file_book:
        file_id = line.split(',')[1]
        if file_id in set_ids:
          result_tokens.write(line)

