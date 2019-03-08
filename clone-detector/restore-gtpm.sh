#!/bin/bash

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
scriptPATH=$(realpath "$0")
rootPATH=$(dirname $scriptPATH)
echo "\e[32m[restore-gtpm.sh] \e[0mrestoring gtpm indexes..."
if [ -d "$rootPATH/gtpmindex" ]; then
   rm -rf $rootPATH/gtpmindex
fi
cp -r $rootPATH/backup_gtpm/gtpmindex $rootPATH/

echo "\e[32m[restore-gtpm.sh] \e[0mgtpmindex restored "

