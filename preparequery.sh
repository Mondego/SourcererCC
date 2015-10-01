#!/bin/bash
counter=0
for queryfile in  `ls query*`
do
  foldername="NODE_"$counter"/query/"
  counter=$((counter+1))
  rm -rf $foldername	
  mkdir -p $foldername
  mv $queryfile $foldername/
  cp sourcerer-cc.properties $foldername/
done
