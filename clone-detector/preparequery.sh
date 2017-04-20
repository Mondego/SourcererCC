#!/bin/bash
num_nodes="${1:-0}"
scriptPATH=`realpath $0`
rootPATH=`dirname $scriptPATH`
echo "rootpath is : $rootPATH"
for i in $(seq 1 1 $num_nodes)
do
  foldername="$rootPATH/NODE_$i/query/"
  rm -rf $foldername
  mkdir -p $foldername
  queryfile="$rootPATH/query_$i.file"
  mv $queryfile $foldername/
  cp $rootPATH/sourcerer-cc.properties "$rootPATH/NODE_"$i/
  cp $rootPATH/res/log4j2.xml "$rootPATH/NODE"_$i/
done
