#!/bin/bash
# Run this script after indexing with just 1 node

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
scriptPATH=$(realpath "$0")
rootPATH=$(dirname $scriptPATH)
printf "\e[32m[move-index.sh] \e[0mInstalling indexes...\n"

if [ -d "$rootPATH/index" ]; then
    rm -rf $rootPATH/index
fi
if [ -d "$rootPATH/fwdindex" ]; then
    rm -rf $rootPATH/fwdindex
fi

mkdir $rootPATH/index
mv $rootPATH/NODE_1/index/shards/* $rootPATH/index
mkdir $rootPATH/fwdindex
mv $rootPATH/NODE_1/fwdindex/shards/* $rootPATH/fwdindex

printf "\e[32m[move-index.sh] \e[0mIndexes installed\n"

