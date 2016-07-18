#!/bin/bash
# run this script on master
echo "merging..."
rootPATH=`pwd`
echo $rootPATH
java -Dproperties.location="$rootPATH/sourcerer-cc.properties" -Xms30g -Xmx30g -XX:+UseCompressedOops -jar dist/indexbased.IndexMerger.jar


