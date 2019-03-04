#!/bin/bash
#
#

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
scriptPATH=$(realpath "$0")
rootPATH=`dirname $scriptPATH`
echo "backing up gtpm indexes..."
rm -rf $rootPATH/backup_gtpm
mkdir $rootPATH/backup_gtpm
cp -r $rootPATH/gtpmindex $rootPATH/backup_gtpm

echo "gtpmindex backup created "


