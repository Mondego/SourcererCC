#!/bin/bash
 
## define an array ##
#arrayname=(cglib dom4j hibernate junit log4j lucene)
## get item count using ${arrayname[@]} ##
for project in $(ls /Users/vaibhavsaini/Dropbox/clonedetection/projects/)
do
  printf "\e[32m[runPostProcessing.sh] \e[0mrunning java -jar dist/postprocessing.ClonesNamesAssembler.jar $project\n"
  java -Xms13g -Xmx13g -jar dist/postprocessing.ClonesNamesAssembler.jar $project
  printf "\e[32m[runPostProcessing.sh] \e[0mrunning java -jar dist/postprocessing.ClonesBugsAssembler.jar $project\n"
  java -Xms13g -Xmx13g -jar dist/postprocessing.ClonesBugsAssembler.jar $project
done
