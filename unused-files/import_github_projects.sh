#!/bin/bash

ls cloneGithub/projects | while read f; do echo projects/${f} >> SourcererCC/tokenizers/#block-level/project-list.txt; done;
mv cloneGithub/projects SourcererCC/tokenizers/block-level