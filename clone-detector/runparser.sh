#!/bin/bash
processes="${1:-3}"
#rm -rf input/dataset
#rm -rf input/bookkeeping
rm numberedProjects*
echo 0 > idgen.txt
echo "f" > idgenstatus.txt
input_file="projects_numbered.txt"
nl -ba -s ',' projects.txt > $input_file

# Work out lines per file.

total_lines=$(wc -l <${input_file})
((lines_per_file = ($total_lines + $processes - 1) / $processes))

# Split the actual file, maintaining lines.

split -l $lines_per_file $input_file numberedProjects.

# Debug information

echo "Total lines     = ${total_lines}"
echo "Lines  per file = ${lines_per_file}"    
wc -l numberedProjects.*

ant cdparse

for c in `ls numberedProjects*`
do
	echo "running java -jar dist/parser.FileParser.jar $c"
	java -Xms1024M -Xmx1024M -Xss100M -jar dist/parser.FileParser.jar $c &
done
echo "done!"
