#!/bin/bash
#
#
# scriptPATH=`realpath $0`
scriptPATH="pwd"
rootPATH=`dirname $scriptPATH`
echo "restoring gtpm indexes..."
if [ -d "$rootPATH/gtpmindex" ]; then
   rm -rf $rootPATH/gtpmindex
fi
cp -r $rootPATH/backup_gtpm/gtpmindex $rootPATH/

echo "gtpmindex restored "


