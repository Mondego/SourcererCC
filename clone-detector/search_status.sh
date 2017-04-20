#!/bin/bash
scriptPATH=`realpath $0`
rootPATH=`dirname $scriptPATH`
num=`cat search_metadata.txt`

for i in $(seq 1 1 $num)
do
 p=`cat SCC_LOGS/NODE_$i/scc.log | grep " RL " | tail -1 | cut -d" " -f8`
 t=`wc -l NODE_$i/query/query* | cut -d" " -f1`
 echo "NODE_$i : $p processed, out of $t."
done
