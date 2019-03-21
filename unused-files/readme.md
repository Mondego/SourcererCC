#Files' purpose:

# run.sh

Runs noindex.CloneDetectorWithFilter many times with different options. No usages found

# mergeindexes.sh

Runs `ant cdmerge` and then *indexbased.IndexMerger class*. Not found usages

# copy\_properties.sh

Copies sourcerer-cc.properties to NODE\_$/ directories and runs `replacenodeprefix.sh`. Not found usages

# cleanup.sh

Removes some files that aren't existing anyway

# analyze.py

Prints some info based on results

## mergeindexes-cygwin.sh, runnodes-cygwin.sh

Same as mergeindexes.sh and runnodes.sh but with paths in windows-way. May be for running in cygwin, but i don't know what for

## cloneGithub.py

Someway filters repositories by having license and writtent in java and other strange things with smth named "siva"

## filterResults.py

Filter copies from results of SourcererCC and finds clones with non-compatible licenses

## step[1-4]:

From original README description:

### step1 - Preparation of the list of projects
### step2 - Assurance of the result of tokenization
### step3 - Find file-hash distinct and unique files
### step4 - Find token-hash distinct and unique files
