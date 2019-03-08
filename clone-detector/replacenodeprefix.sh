#!/bin/bash

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
scriptPATH=$(realpath "$0")
rootPATH=$(dirname $scriptPATH)
num_nodes="${1:-0}"
src_text="NODE_PREFIX=NODE"
src_log4_text="NODE_"
for i in  $(seq 1 1 $num_nodes)
do
  foldername="$rootPATH/NODE_"$i
  replace_text="NODE_PREFIX=NODE_"$i
  replace_log4_text="NODE_"$i

  printf "\e[32m[replacenodeprefix.sh] \e[0m$foldername\n"
  printf "\e[32m[replacenodeprefix.sh] \e[0m$replace_text\n"
  printf "\e[32m[replacenodeprefix.sh] \e[0m$replace_log4_text\n"

  # replace NODE_PREFIX declaration
  sed -i -e "s/$src_text/$replace_text/g" $foldername/sourcerer-cc.properties

  # inline NODE_PREFIX usages
  sed -i -e "s/\${NODE_PREFIX}/NODE_$i/g" $foldername/sourcerer-cc.properties

  # replace logger configs
  sed -i -e "s/$src_log4_text/$replace_log4_text/g" $foldername/log4j2.xml
done
