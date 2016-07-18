#!/bin/bash
# run this script on master
ant clean cdi
num_nodes="${1:-50}"
threshold="${2:-8}"
echo "indexing on master"
rootPATH=`pwd`
echo $rootPATH

#for i in $(seq 1 1 $num_nodes)
#do
# echo "init on node00$i"
 java -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Xms2g -Xmx2g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar init $threshold &
 #java -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Xms2g -Xmx2g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar index $threshold &
 #java -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Xms2g -Xmx2g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar search $threshold &
#done
