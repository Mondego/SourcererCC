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

mkdir index
mv NODE_1/index/shards/* index
mkdir fwdindex
mv NODE_1/fwdindex/shards/* fwdindex

echo "Indexes installed"


