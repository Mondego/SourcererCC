#!/bin/bash
num_nodes="${1:-6}"
src_text="NODE_PREFIX=NODE"
for i in  $(seq 1 1 $num_nodes)
do
  foldername="NODE_"$i
  replace_text="NODE_PREFIX=NODE_"$i

  echo $foldername
echo $replace_text

  sed -i -e "s/$src_text/$replace_text/g" $foldername/sourcerer-cc.properties
done
