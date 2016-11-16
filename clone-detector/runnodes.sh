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

PIDS=""
for i in $(seq 1 1 $num_nodes)
do
    java -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Dlog4j.configurationFile="$rootPATH/NODE_$i/log4j2.xml" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar $mode $threshold &
    PIDS+="$! "
done
echo $PIDS
# wait for the processes to get over
status=0
count=1
for pid in $PIDS
do
    wait $pid
    if [ $? -eq 0 ]; then
        echo " NODE_$count SUCCESS - Job $pid exited with a status of $?"
    else
        echo "NODE_$count FAILED - Job $pid exited with a status of $?"
        status=1
    fi
    count=$((count+1))
done
exit $status