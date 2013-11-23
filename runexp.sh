#!/bin/bash
 
## define an array ##
#arrayname=(cglib dom4j hibernate junit log4j lucene)
loops="${1:-1}"
## get item count using ${arrayname[@]} ##
for project in `ls /Users/vaibhavsaini/Dropbox/clonedetection/projects/`
do
  echo "executing run.sh $loops $project"
  bash run.sh $loops $project
  # do something on $m #
done
