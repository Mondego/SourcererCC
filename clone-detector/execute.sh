#!/bin/bash
scriptPATH=`realpath $0`
rootPATH=`dirname $scriptPATH`
echo $rootPATH
rm -rf $rootPATH/NODE*
num_nodes="${1:-2}"
#num_nodes=$((num_nodes-1))
th="${2:-8}"
queryfile="$rootPATH/input/dataset/blocks.file"
echo "spliting query file $queryfile into $num_nodes parts"
python $rootPATH/unevensplit.py $queryfile $num_nodes
echo "moving files"
bash $rootPATH/preparequery.sh $num_nodes
echo "done!"
bash $rootPATH/replacenodeprefix.sh $num_nodes

