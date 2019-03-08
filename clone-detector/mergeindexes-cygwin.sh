#!/bin/bash

# Run this script after indexing with several nodes
ant cdmerge
echo "\e[32m[mergeindexes-cygwin.sh] \e[0mmerging..."
unixPATH=$(pwd)
echo "\e[32m[mergeindexes-cygwin.sh] \e[0m" $unixPATH
p=$(cygpath -aw $unixPATH/sourcerer-cc.properties)
echo "\e[32m[mergeindexes-cygwin.sh] \e[0m" $p
java -Dproperties.location="$p" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.IndexMerger.jar

