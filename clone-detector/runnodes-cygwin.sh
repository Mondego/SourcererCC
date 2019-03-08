#!/bin/bash

# run this script on master
ant clean cdi
mode="${1:-search}"
num_nodes="${2:-50}"
echo $num_nodes > search_metadata.txt
threshold="${3:-8}"
printf "\e[32m[runnodes-cygwin.sh] \e[0m*****************************************************\n"
printf "\e[32m[runnodes-cygwin.sh] \e[0mrunning this script in $mode mode\n"
printf "\e[32m[runnodes-cygwin.sh] \e[0m*****************************************************\n"

printf "\e[32m[runnodes-cygwin.sh] \e[0mindexing on master\n"
unixPath=$(pwd)
rootPATH=$(cygpath -aw $unixPath)
printf "\e[32m[runnodes-cygwin.sh] \e[0m$rootPATH\n"

for i in $(seq 1 1 $num_nodes)
do
 p=$(cygpath -aw $unixPath/NODE_$i/sourcerer-cc.properties)
 java -Dproperties.location="$p" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar $mode $threshold &
done
