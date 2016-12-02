#!/bin/bash

#N="${1:-2}" # default is 2. N is number of files needed
#lines_per_part = int(total_lines + $N - 1) / $N

# Configuration stuff
scriptPATH=`realpath $0`
rootPATH=`dirname $scriptPATH`
echo "inside splitquery "
queryfile="$rootPATH/input/dataset/blocks.file"
num_files="${1:-2}"

# Work out lines per file.

total_lines=$(wc -l <${queryfile})
((lines_per_file = ($total_lines + $num_files - 1) / $num_files))

# Split the actual file, maintaining lines.

split -l $lines_per_file $queryfile $rootPATH/query.

# Debug information

echo "Total lines     = ${total_lines}"
echo "Lines  per file = ${lines_per_file}"    
wc -l $rootPATH/query.*
