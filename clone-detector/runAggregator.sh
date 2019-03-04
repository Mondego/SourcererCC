#!/bin/bash
 
## define an array ##
#arrayname=(cglib dom4j hibernate junit log4j lucene)
## get item count using ${arrayname[@]} ##
for th in  7.5 8 8.5 9 9.5 10
#for th in 8
do
  for project in `ls /Users/vaibhavsaini/Dropbox/clonedetection/projects/`
  do
    echo "running java -jar dist/postprocessing.Aggregator.jar $project $th"
    java -Xms13g -Xmx13g -jar dist/postprocessing.Aggregator.jar $project $th
  done
done
