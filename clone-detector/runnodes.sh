#!/bin/bash

# run this script on master
realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
scriptPATH=$(realpath "$0")
rootPATH=$(dirname $scriptPATH)
printf "\e[32m[runnodes.sh] \e[0m$rootPATH\n"
ant -buildfile $rootPATH/build.xml clean cdi
mode="${1:-search}"
num_nodes="${2:-50}"
threshold="${3:-8}"
printf "\e[32m[runnodes.sh] \e[0m*****************************************************\n"
printf "\e[32m[runnodes.sh] \e[0mrunning this script in $mode mode\n"
printf "\e[32m[runnodes.sh] \e[0m*****************************************************\n"

echo $num_nodes > $rootPATH/search_metadata.txt

PIDS=""

rm -f "$rootPATH/nodes_completed.txt"

for i in $(seq 1 1 $num_nodes)
do
    java -Dproperties.rootDir="$rootPATH/" -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Dlog4j.configurationFile="$rootPATH/NODE_$i/log4j2.xml" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar $rootPATH/dist/indexbased.SearchManager.jar $mode $threshold &
    PIDS+="$! "
done
printf "\e[32m[runnodes.sh] \e[0m$PIDS\n"
# wait for the processes to get over
status=0
count=1
for pid in $PIDS
do
    wait $pid
    if [ $? -eq 0 ]; then
        printf "\e[32m[runnodes.sh] \e[0mNODE_$count SUCCESS - Job $pid exited with a status of $?\n"
    else
        printf "\e[32m[runnodes.sh] \e[0mNODE_$count FAILED - Job $pid exited with a status of $?\n"
        status=1
    fi
    count=$((count+1))
done
exit $status
