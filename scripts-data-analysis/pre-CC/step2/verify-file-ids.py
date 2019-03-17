# Checks that file id's are in fact unique.
# This is just an assurance because finding later in
# the pipeline that some files had the same id means
# repeating everything

# Usage: python this-script.py [files_stats or files_tokens ]

import sys
import os

set_ids = set()
lines = 0

for stats_file in os.listdir(sys.argv[1]):
    if stats_file.endswith('.stats'):
        print('Reading ', stats_file)
        stats_file = os.path.join(sys.argv[1], stats_file)
        print('Searching on ', stats_file)
        with open(stats_file, 'r') as file_book:
            for line in file_book:
                file_id = line.split(',')[1]
                lines += 1
                if file_id in set_ids:
                    print('ERROR for file id:', file_id)
                else:
                    set_ids.add(file_id)

print('## NUMBERS BELOW MUST MATCH ##')
print('Number of unique ids:', len(set_ids))
print('Number of files:     ', lines)
