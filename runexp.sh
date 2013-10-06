#!/bin/bash
 
## define an array ##
arrayname=(cocoon ANT maven lucene hadoop)
loops="${1:-1}"
## get item count using ${arrayname[@]} ##
for m in "${arrayname[@]}"
do
  echo "executing run.sh $loops $m"
  bash run.sh $loops $m
  # do something on $m #
done
