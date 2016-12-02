#!/bin/bash
scriptPATH=`realpath $0`
rootPATH=`dirname $scriptPATH`
counter=1
for queryfile in  `ls $rootPATH/query*`
do
  foldername="$rootPATH/NODE_"$counter"/query/"
  rm -rf $foldername
  mkdir -p $foldername
  mv $queryfile $foldername/
  cp $rootPATH/sourcerer-cc.properties "$rootPATH/NODE_"$counter/
  cp $rootPATH/res/log4j2.xml "$rootPATH/NODE"_$counter/
  counter=$((counter+1))
done
