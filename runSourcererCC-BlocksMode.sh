#!/bin/bash

set -e

mkdir -p clone-detector/input/dataset/
cd tokenizers/block-level/
python tokenizer.py zipblocks | while read line; do printf "\e[32m[tokenizer.py zipblocks] \e[0m$line\n"; done
cat blocks_tokens/* > ../../clone-detector/input/dataset/blocks.file
cd ../../clone-detector
python controller.py 1 | while read line; do printf "\e[32m[controller.py 1] \e[0m$line\n"; done
cd ..
cat clone-detector/NODE_*/output8.0/query_* > results.pairs
