#!/bin/bash
scriptPATH=`realpath $0`
rootPATH=`dirname $scriptPATH`
echo $rootPATH
rm -rf $rootPATH/NODE*
num_nodes="${1:-2}"
#num_nodes=$((num_nodes-1))
th="${2:-8}"
echo "spliting query file"
bash $rootPATH/splitquery.sh $num_nodes
echo "moving files"
bash $rootPATH/preparequery.sh
echo "done!"
bash $rootPATH/replacenodeprefix.sh $num_nodes
