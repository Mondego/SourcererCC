#!/bin/bash

#echo -n "" > tokenizers/block-level/project-list.txt
#ls cloneGithub/projects | while read f; do echo projects/${f} >> SourcererCC/tokenizers/#block-level/project-list.txt; done;
#rm -r SourcererCC/tokenizers/block-level/projects
#mkdir SourcererCC/tokenizers/block-level/projects
#mv cloneGithub/projects SourcererCC/tokenizers/block-level
cd tokenizers/file-level/
rm -rf files_tokens
rm -rf bookkeeping_projs
rm -rf file_block_stats
rm -rf logs
python tokenizer.py zip | while read line; do printf "\e[32m[tokenizer.py zip] \e[0m$line\n"; done
cat files_tokens/* > blocks.file
mkdir ../../clone-detector/input
mkdir ../../clone-detector/input/dataset
cp blocks.file ../../clone-detector/input/dataset/
cd ../../clone-detector
python controller.py 1 | while read line; do printf "\e[32m[controller.py 1] \e[0m$line\n"; done
cd ..
cat clone-detector/NODE_*/output8.0/query_* > results.pairs
