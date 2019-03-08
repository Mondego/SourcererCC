#!/bin/bash

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
scriptPATH=$(realpath "$0")
rootPATH=$(dirname $scriptPATH)
echo "\e[32m[execute.sh] \e[0m" $rootPATH
rm -rf $rootPATH/NODE*
num_nodes="${1:-2}"
th="${2:-8}"
queryfile="$rootPATH/input/dataset/blocks.file"
echo "\e[32m[execute.sh] \e[0mspliting query file $queryfile into $num_nodes parts"
python $rootPATH/unevensplit.py $queryfile $num_nodes
echo "\e[32m[execute.sh] \e[0mmoving files"
bash $rootPATH/preparequery.sh $num_nodes
echo "\e[32m[execute.sh] \e[0mdone!"
bash $rootPATH/replacenodeprefix.sh $num_nodes

