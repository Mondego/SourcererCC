#!/bin/bash
ant cdmeta
rootPATH=`pwd`

java -Dproperties.location="$rootPATH/sourcerer-cc.properties" -Xms6g -Xmx6g -XX:+UseCompressedOops -jar dist/medianbased.CloneDetector.jar
