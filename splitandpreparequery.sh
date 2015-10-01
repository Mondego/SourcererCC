#!/bin/bash
rm -rf NODE*
num_nodes="${1:-4}"
echo "spliting query file"
bash ./splitquery.sh $num_nodes
echo "moving files"
bash ./preparequery.sh
echo "done!"
