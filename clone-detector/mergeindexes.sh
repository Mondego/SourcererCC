#!/bin/bash

# Run this script after indexing with several nodes

ant cdmerge
echo "\e[32m[mergeindexes.sh] \e[0mmerging..."
rootPATH=$(pwd)
echo "\e[32m[mergeindexes.sh] \e[0m" $rootPATH
java -Dproperties.location="$rootPATH/sourcerer-cc.properties" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.IndexMerger.jar

