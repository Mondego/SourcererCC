#!/bin/bash
#
# Run this script after indexing with several nodes
#

ant cdmerge
echo "merging..."
rootPATH=`pwd`
echo $rootPATH
java -Dproperties.location="$rootPATH/sourcerer-cc.properties" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/indexbased.IndexMerger.jar


