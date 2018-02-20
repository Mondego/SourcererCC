#!/bin/bash
rm Log_*
rm -rf *index
mv input/dataset/oldData/*.file input/dataset/blocks.file
rm -rf input/dataset/oldData
rm scriptinator_metadata.scc
rm -rf SCC_LOGS
rm -rf test
