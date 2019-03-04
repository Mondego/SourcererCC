#!/bin/bash

#echo -n "" > tokenizers/block-level/project-list.txt
#ls cloneGithub/projects | while read f; do echo projects/${f} >> SourcererCC/tokenizers/#block-level/project-list.txt; done;
#rm -r SourcererCC/tokenizers/block-level/projects
#mkdir SourcererCC/tokenizers/block-level/projects
#mv cloneGithub/projects SourcererCC/tokenizers/block-level
rm clone-detector/search_metadata.txt
cd tokenizers/block-level/
rm -rf blocks_tokens
rm -rf bookkeeping_projs
rm blocks.file
rm -rf file_block_stats
rm -rf logs
rm extractJavaFunction.pyc
rm extractPythonFunction.pyc
python tokenizer.py zipblocks
cat blocks_tokens/* > blocks.file
cp blocks.file ../../clone-detector/input/dataset/
cd ../../clone-detector
python controller.py
cd ..
cat clone-detector/NODE_*/output8.0/query_* > results.pairs