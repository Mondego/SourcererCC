#Checks that file id's are in fact unique.
#This is just an assurance because finding later in
# the pipeline that some files had the same id means
# repeating everything

#Usage: python this-script.py [files_stats or files_tokens ]

import sys
import os

set_ids = set()
lines = 0

for file in os.listdir(sys.argv[1]):
  if file.endswith('.stats'):
    print 'Reading ',file
    file = os.path.join(sys.argv[1],file)
    print 'Searching on ',file
    with open(file,'r') as file_book:
      for line in file_book:
        file_id = line.split(',')[1]
        lines += 1
        if file_id in set_ids:
          print 'ERROR for file id:',file_id
        else:
          set_ids.add(file_id)

print '## NUMBERS BELOW MUST MATCH ##'
print 'Number of unique ids:',len(set_ids)
print 'Number of files:     ',lines


