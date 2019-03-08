#!/bin/bash

# run this script on master
ant clean cdi
mode="${1:-search}"
num_nodes="${2:-50}"
echo $num_nodes > search_metadata.txt
threshold="${3:-8}"
echo "\e[32m[runnodes-cygwin.sh] \e[0m*****************************************************"
echo "\e[32m[runnodes-cygwin.sh] \e[0mrunning this script in $mode mode"
echo "\e[32m[runnodes-cygwin.sh] \e[0m*****************************************************"

echo "\e[32m[runnodes-cygwin.sh] \e[0mindexing on master"
unixPath=$(pwd)
rootPATH=$(cygpath -aw $unixPath)
echo "\e[32m[runnodes-cygwin.sh] \e[0m" $rootPATH

for i in $(seq 1 1 $num_nodes)
do
 p=$(cygpath -aw $unixPath/NODE_$i/sourcerer-cc.properties)
 java -Dproperties.location="$p" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.SearchManager.jar $mode $threshold &
done
