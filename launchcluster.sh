#!/bin/bash
num_nodes="${1:-10}"
cluster_name="sdx$num_nodes"
echo "$num_nodes $cluster_name"
src_text="CLUSTER_SIZE *= *[0-9]*"
ABSOLUTE_PATH=$(cd ~/; pwd)
foldername=$ABSOLUTE_PATH/.starcluster
echo "foldername : $foldername"
replace_text="CLUSTER_SIZE = $num_nodes"
sed -i -e "s/$src_text/$replace_text/g" $foldername/config
echo "sed -i -e 's/$src_text/$replace_text/g' $foldername/config"
echo "CLUSTER SIZE updated, launching cluster $cluster_name with $num_nodes nodes"
starcluster start $cluster_name
echo "xfering files to cluster $cluster_name"

src_text="num_nodes=[0-9][0-9]*"
replace_text="num_nodes=$num_nodes"
sed -i -e "s/$src_text/$replace_text/g" execute.sh

bash ./xfer2cluster.sh $cluster_name
echo "executing ./execute.sh $num_nodes"
echo "starcluster sshmaster $cluster_name 'cd /home ; bash ./execute.sh'"
starcluster sshmaster $cluster_name 'cd /home ; bash ./execute.sh'
