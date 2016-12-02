#!/bin/bash
#
#
echo "restoring gtpm indexes..."
if [ -d "gtpmindex" ]; then
   rm -rf gtpmindex
fi
cp -r backup_gtpm/gtpmindex ./

echo "gtpmindex restored "


