#!/bin/bash
# run this script on master

num_nodes="${1:-6}"
threshold="${2:-8}"
echo "indexing on master"
java -Dproperties.location="/home/NODE_1/sourcerer-cc.properties" -Xms14g -Xmx14g  -jar dist/indexbased.SearchManager.jar index $threshold

for i in $(seq 1 1 $num_nodes)
do
 script="cd /home ; java -Dproperties.location=/home/NODE_$i/sourcerer-cc.properties -Xms10g -Xmx10g -jar dist/indexbased.SearchManager.jar search $threshold > /home/NODE_$i/out.log 2>/home/NODE_$i/error.log"
 echo "script is $script"
 if [ $i -gt 9 ]
 then
   echo "starting search on node0$i"
   ssh "node0$i" "$script" &
 else
  echo "starting search on node00$i"
   ssh  "node00$i" "$script" &
 fi
done
#echo "starting search on master"
#java -Dproperties.location="/home/NODE_0/query/sourcerer-cc.properties" -Xms512m -Xmx512m -jar dist/indexbased.SearchManager.jar search $threshold &

echo "all searches running"
