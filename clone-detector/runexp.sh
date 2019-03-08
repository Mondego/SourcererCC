#!/bin/bash

bash ant cdboth
## define an array ##
#arrayname=(cglib dom4j hibernate junit log4j lucene)
loops="${1:-1}"
## get item count using ${arrayname[@]} ##
for th in  8
do
    for project in $(cat pro.txt)
  do
    printf "\e[32m[runexp.sh] \e[0mexecuting runn.sh $loops $project $th\n"
    bash runn.sh $loops $project $th
  done
done
