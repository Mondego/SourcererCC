#!/bin/bash

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
scriptPATH=$(realpath "$0")
rootPATH=$(dirname $scriptPATH)
printf "\e[32m[backup-gtpm.sh] \e[0mbacking up gtpm indexes...\n"
rm -rf $rootPATH/backup_gtpm
mkdir $rootPATH/backup_gtpm
cp -r $rootPATH/gtpmindex $rootPATH/backup_gtpm

printf "\e[32m[backup-gtpm.sh] \e[0mgtpmindex backup created\n"

