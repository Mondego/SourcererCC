#!/bin/bash
#
# Run this script after indexing with just 1 node
#
echo "Installing indexes..."

if [ -d "index" ]; then
    rm -rf index
fi
if [ -d "fwdindex" ]; then
    rm -rf fwdindex
fi

mv NODE_1/index .
mv NODE_1/fwdindex .

echo "Indexes installed"


