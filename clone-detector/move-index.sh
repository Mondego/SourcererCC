#!/bin/bash
#
# Run this script after indexing with just 1 node
#
scriptPATH=`realpath $0`
rootPATH=`dirname $scriptPATH`
echo "Installing indexes..."

if [ -d "$rootPATH/index" ]; then
    rm -rf $rootPATH/index
fi
if [ -d "$rootPATH/fwdindex" ]; then
    rm -rf $rootPATH/fwdindex
fi

mkdir $rootPATH/index
mv $rootPATH/NODE_1/index/shards/* $rootPATH/index
echo "Indexes installed"


