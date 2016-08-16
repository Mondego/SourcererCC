# SourcererCC can, in some situations, produce repeated clone pairs
# This script cleans the results of SourcererCC and ensures no repetition

# Usage
# $ python step1-remove-duplicated.py SourcererCC-results.txt > SourcererCC-results-norepetition.txt

import sys

res = set()

with open(sys.argv[1],'r') as file:
        for line in file:
                res.add(line[:-1])

for a in res:
        print a
