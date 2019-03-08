#!/bin/bash

# Run this script after indexing with several nodes
ant cdmerge
printf "\e[32m[mergeindexes.sh] \e[0mmerging...\n"
rootPATH=$(pwd)
printf "\e[32m[mergeindexes.sh] \e[0m$rootPATH\n"
java -Dproperties.location="$rootPATH/sourcerer-cc.properties" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.IndexMerger.jar

