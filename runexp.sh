#!/bin/bash
 
## define an array ##
#arrayname=(cglib dom4j hibernate junit log4j lucene)
loops="${1:-1}"
## get item count using ${arrayname[@]} ##
for th in 10
do
  for project in `ls /Users/vaibhavsaini/Dropbox/clonedetection/projects-test/`

  do
    echo "executing run.sh $loops $project $th"
    bash run.sh $loops $project $th
    # do something on $m #
  done
done
