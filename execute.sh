#!/bin/bash
rm -rf NODE*
num_nodes=3
num_nodes=$((num_nodes-1))
th="${2:-7}"
echo "spliting query file"
bash ./splitquery.sh $num_nodes
echo "moving files"
bash ./preparequery.sh
echo "done!"
bash ./replacenodeprefix.sh $num_nodes
echo "calling runnodes.sh"
bash ./runnodes.sh $num_nodes $th
