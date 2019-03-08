#!/bin/bash

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
scriptPATH=$(realpath "$0")
rootPATH=$(dirname $scriptPATH)
printf "\e[32m[execute.sh] \e[0m\n$rootPATH\n"
rm -rf $rootPATH/NODE*
num_nodes="${1:-2}"
th="${2:-8}"
queryfile="$rootPATH/input/dataset/blocks.file"
printf "\e[32m[execute.sh] \e[0mspliting query file $queryfile into $num_nodes parts\n"
python $rootPATH/unevensplit.py $queryfile $num_nodes
printf "\e[32m[execute.sh] \e[0mmoving files\n"
bash $rootPATH/preparequery.sh $num_nodes
printf "\e[32m[execute.sh] \e[0mdone!\n"
bash $rootPATH/replacenodeprefix.sh $num_nodes

