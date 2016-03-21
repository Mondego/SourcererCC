#!/bin/bash
# run this script on master
git pull origin leidos
ant clean cdi
num_nodes="${1:-14}"
threshold="${2:-8}"
echo "indexing on master"
java -Dproperties.location="/raid5/clopes/C++_tokens/RUN/SourcererCC/NODE_1/sourcerer-cc.properties" -Xms10g -Xmx10g  -jar dist/indexbased.SearchManager.jar index $threshold

for i in $(seq 1 1 $num_nodes)
do
 echo "starting search on node00$i"
 java -Dproperties.location="/raid5/clopes/C++_tokens/RUN/SourcererCC/NODE_$i/sourcerer-cc.properties" -Xms2g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar search $threshold &
done
#echo "starting search on master"
#java -Dproperties.location="/home/NODE_0/query/sourcerer-cc.properties" -Xms512m -Xmx512m -jar dist/indexbased.SearchManager.jar search $threshold &

echo "all searches running"
