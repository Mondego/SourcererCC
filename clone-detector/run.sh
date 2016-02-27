#!/bin/bash
loops="${1:-1}"
prefix="${2:-codeclonedetection}"
th="${3:-8}"

# naive with overlap
for ((c=1;c<=$loops;c++))
do
	echo "running java -jar dist/noindex.CloneDetector.jar $prefix $th overlap"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetector.jar $prefix $th overlap
done

# naive with jaccard
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetector.jar $prefix $th jaccard"
    java -Xms4g -Xmx4g -jar dist/noindex.CloneDetector.jar $prefix $th jaccard
done

# with prefix overlap
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_none overlap"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_none overlap
done

# with prefix and jaccard
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_none jaccard"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_none jaccard
done

# with prefix and position
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_cv overlap"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_cv overlap
done


# with prefix and position with jaccard
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_cv jaccard"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_cv jaccard
done

# with prefix and position_at_candidate with overlap
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_c overlap"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_c overlap
done

# with prefix and position_at_candidate with jaccard
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_c jaccard"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_c jaccard
done

# with prefix and position_at_validation with overlap
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_v overlap"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_v overlap
done

# with prefix and position_at_validation with jaccard
for ((c=1;c<=$loops;c++))
do
    echo "running java -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_v jaccard"
	java -Xms4g -Xmx4g -jar dist/noindex.CloneDetectorWithFilter.jar $prefix $th 0 pos_filter_v jaccard
done