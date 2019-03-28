#!/bin/bash

# Run this script after indexing with several nodes
ant cdmerge
printf "\e[32m[mergeindexes-cygwin.sh] \e[0mmerging...\n"
unixPATH=$(pwd)
printf "\e[32m[mergeindexes-cygwin.sh] \e[0m$unixPATH\n"
p=$(cygpath -aw $unixPATH/sourcerer-cc.properties)
printf "\e[32m[mergeindexes-cygwin.sh] \e[0m$p\n"
java -Dproperties.location="$p" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.IndexMerger.jar

