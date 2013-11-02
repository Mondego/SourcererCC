#!/bin/bash
loops="${1:-1}"
prefix="${2:-hadoop}"
for ((c=1;c<=$loops;c++))
do
	echo "running java -jar dist/CloneDetectorWithFilter.jar $prefix"
	java -Xms13g -Xmx13g -jar dist/CloneDetectorWithFilter.jar $prefix
	echo "running java -jar dist/CloneDetector.jar $prefix"
	java -Xms13g -Xmx13g -jar dist/CloneDetector.jar $prefix
done
