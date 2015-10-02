#!/bin/bash
num_nodes="${1:-6}"
counter=0
src_text="CLUSTER_SIZE *= *[0-9]*"
ABSOLUTE_PATH=$(cd ~/; pwd)
foldername=$ABSOLUTE_PATH/.starcluster
echo "foldername : $foldername"
replace_text="CLUSTER_SIZE = $num_nodes"
sed -i -e "s/$src_text/$replace_text/g" $foldername/config
echo "sed -i -e 's/$src_text/$replace_text/g' $foldername/config"
echo "CLUSTER SIZE updated, launching cluster sdx with $num_nodes nodes"
starcluster start sdx2
