#!/bin/bash
 
## define an array ##
#arrayname=(cglib dom4j hibernate junit log4j lucene)
loops="${1:-1}"
## get item count using ${arrayname[@]} ##
for th in  6 6.5 7 7.5 8 8.5 9 9.5
do
  for project in `ls /home/saini/Dropbox/clonedetection/projects/`
  do
    echo "executing run.sh $loops $project $th"
    bash run.sh $loops $project $th
    # do something on $m #
  done
done
