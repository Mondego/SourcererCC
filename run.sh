#!/bin/bash
loops="${1:-1}"
prefix="${2:-codeclonedetection}"
th="${3:-8}"
for ((c=1;c<=$loops;c++))
do
	echo "running java -jar dist/CloneDetectorWithFilter.jar $prefix $th"
	java -Xms13g -Xmx13g -jar dist/CloneDetectorWithFilter.jar $prefix $th
	echo "running java -jar dist/CloneDetector.jar $prefix $th"
	java -Xms13g -Xmx13g -jar dist/CloneDetector.jar $prefix $th
done
