#!/bin/bash

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
num=$(cat search_metadata.txt)

for i in $(seq 1 1 $num)
do
 p=$(grep " RL " SCC_LOGS/NODE_$i/scc.log | tail -1 | cut -d" " -f8)
 t=$(wc -l NODE_$i/query/query* | cut -d" " -f1)
 printf "\e[32m[search_status.sh] \e[0mNODE_$i : $p processed, out of $t\n"
done
