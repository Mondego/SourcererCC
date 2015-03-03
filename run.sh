#!/bin/bash
loops="${1:-1}"
prefix="${2:-codeclonedetection}"
th="${3:-8}"
for ((c=1;c<=$loops;c++))
do
	echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th
	echo "running java -jar dist/noindex.CloneDetector.jar $prefix $th"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetector.jar $prefix $th
done
