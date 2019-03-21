# Creates a group of files whose file-hash is unique
# Usage $pythoon this-script.py files_stats/ files_tokens/

import sys
import os
import csv
from collections import Counter

PATH_unique_file_hashes = 'unique-file-hashes'

if os.path.exists(PATH_unique_file_hashes):
    print('ERROR - Folder [' + PATH_unique_file_hashes + '] already exists!')
    sys.exit()
else:
    os.makedirs(PATH_unique_file_hashes)

set_ids = set()
dict_hashes_frequency = Counter()

for stats_file in os.listdir(sys.argv[1]):
    if stats_file.endswith('.stats'):
        filename = os.path.join(sys.argv[1], stats_file)
        print('Searching on ', filename)
        with open(filename, 'r') as file_book:
            csv_reader = csv.reader(file_book, delimiter=',')
            for entry in csv_reader:
                file_hash = entry[4]
                dict_hashes_frequency[file_hash] += 1

with open(os.path.join(PATH_unique_file_hashes, 'unique-files.stats'), 'a+') as result_stats:
    for stats_file in os.listdir(sys.argv[1]):
        if stats_file.endswith('.stats'):
            filename = os.path.join(sys.argv[1], stats_file)
            print('Searching on ', filename)
            with open(filename, 'r') as file_book:
                csv_reader = csv.reader(file_book, delimiter=',')
                for entry in csv_reader:
                    file_hash = entry[4]
                    if dict_hashes_frequency[file_hash] == 1:
                        result_stats.write(','.join(entry) + '\n')
                        set_ids.add(entry[1])

with open(os.path.join(PATH_unique_file_hashes, 'unique-files.tokens'), 'a+') as result_tokens:
    for tokens_file in os.listdir(sys.argv[2]):
        if tokens_file.endswith('.tokens'):
            filename = os.path.join(sys.argv[2], tokens_file)
            print('Searching on ', filename)
            with open(filename, 'r') as file_book:
                for line in file_book:
                    file_id = line.split(',')[1]
                    if file_id in set_ids:
                        result_tokens.write(line)
