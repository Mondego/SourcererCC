#!/bin/bash
# run this script on master

num_nodes="${1:-6}"
threshold="${2:-7}"
echo "indexing on master"
java -Dproperties.location="/home/saini/code/repos/SourcererCC/NODE_1/sourcerer-cc.properties" -Xms4g -Xmx4g  -jar dist/indexbased.SearchManager.jar index $threshold

for i in $(seq 1 1 $num_nodes)
do
 echo "starting search on node00$i"
 java -Dproperties.location="/home/saini/code/repos/SourcererCC/NODE_$i/sourcerer-cc.properties" -Xms4g -Xmx4g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar search $threshold &
done
#echo "starting search on master"
#java -Dproperties.location="/home/NODE_0/query/sourcerer-cc.properties" -Xms512m -Xmx512m -jar dist/indexbased.SearchManager.jar search $threshold &

echo "all searches running"
