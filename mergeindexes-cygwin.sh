#!/bin/bash
# run this script on master
ant cdmerge
echo "merging..."
unixPATH=`pwd`
echo $unixPATH
p=`cygpath -aw $unixPATH/sourcerer-cc.properties`
echo $p
java -Dproperties.location="$p" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.IndexMerger.jar


