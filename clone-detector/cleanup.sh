#!/bin/bash

printf "\e[32m[cleanup.sh]\e[0m\n"
rm Log_*
rm -rf *index
rm -rf input/dataset/oldData
rm scriptinator_metadata.scc
