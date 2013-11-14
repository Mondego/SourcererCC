#!/bin/bash
 
## define an array ##
arrayname=(cglib dom4j hibernate junit log4j lucene)
## get item count using ${arrayname[@]} ##
for m in "${arrayname[@]}"
do
  echo "running java -jar dist/postprocessing.ClonesNamesAssembler.jar $m"
  java -Xms13g -Xmx13g -jar dist/postprocessing.ClonesNamesAssembler.jar $m
  echo "running java -jar dist/postprocessing.ClonesBugsAssembler.jar $m"
  java -Xms13g -Xmx13g -jar dist/postprocessing.ClonesBugsAssembler.jar $m
done
