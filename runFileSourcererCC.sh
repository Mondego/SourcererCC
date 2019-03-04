#!/bin/bash

rm clone-detector/search_metadata.txt
cd tokenizers/file-level/
rm -rf blocks_tokens
rm -rf bookkeeping_projs
rm blocks.file
rm -rf file_block_stats
rm -rf logs
rm extractJavaFunction.pyc
rm extractPythonFunction.pyc
python tokenizer.py zip
cat files_tokens/* > blocks.file
cp blocks.file ../../clone-detector/input/dataset/
cd ../../clone-detector
python controller.py
cd ..
cat clone-detector/NODE_*/output8.0/query_* > results.pairs
