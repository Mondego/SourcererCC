#Finds how many projects contribute to files with unique file-hash
#Usage: $python this-script.py unique-file-hashes/unique-files-stats.stats

import os
import sys
import csv

set_projects = set()

with open(sys.argv[1],'r') as file_book:
  for line in file_book:
    set_projects.add(line.split(',')[0])

print 'Number of projects:      ',len(set_projects)

