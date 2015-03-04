#!/bin/bash
loops="${1:-1}"
prefix="${2:-codeclonedetection}"
th="${3:-8}"

# naive
for ((c=1;c<=$loops;c++))
do
	echo "running java -jar dist/noindex.CloneDetector.jar $prefix $th false"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetector.jar $prefix $th false
done

# naive with jaccard
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetector.jar $prefix $th true"
    java -Xms4g -Xmx4g -jar dist/noindex.CloneDetector.jar $prefix $th true
done

# with prefix
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 true false"
    java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 true false
done

# with prefix and jaccard
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 true true"
    java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 true true
done

# with prefix and position
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 false false"
    java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 false false
done


# with prefix and position with jaccard
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 false true"
    java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 false true
done