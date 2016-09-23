#!/bin/bash
num_nodes="${1:-0}"
for i in $(seq 1 1 $num_nodes)
do
 cp sourcerer-cc.properties "NODE_"$i/
done
bash ./replacenodeprefix.sh $num_nodes
