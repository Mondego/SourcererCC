#!/bin/bash
# run this script on master
ant clean cdi
mode="${1:-search}"
num_nodes="${2:-50}"
echo $num_nodes > search_metadata.txt
threshold="${3:-8}"
echo "*****************************************************"
echo "running this script in $mode mode"
echo "*****************************************************"

echo "indexing on master"
rootPATH=`pwd`
echo $rootPATH

for i in $(seq 1 1 $num_nodes)
do
 java -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar $mode $threshold &
done
