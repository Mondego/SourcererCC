# Create sharding bins for multi-tiers
# Given a list of tokenized files, produces shards with the following two restrictions:
# RULE #1. a file will not appear in more than two shard bins
# RULE #2. number of files in a shard bin will not exceed a threshold, defined by INTERVAL_SIZE

# Note: 
# the similarity threshold is now a parameter too, SIMILARITY, to accomodate needs of different layers

# another parameter is the level number, LEVEL_NUM, for now:
# level #1: tokens,
# level #2: uniquetokens,
# level #3: separators,
# level #4: assignments,
# level #5: statements,
# level #6: expressions,
# level #7: thash

# FILTER_THRESHOLD is the same small-file filter defined for CC.

# Usage: $python this-script.py list-of-tokenixed-files.tokens SIMILARITY INTERVAL_SIZE LEVEL_NUM FILTER_THRESHOLD

import sys

FILE = sys.argv[1]
INTERVAL_SIZE = sys.argv[2]
LEVEL_NUM = sys.argv[3]
FILTER_THRESHOLD = argv[4]


print '1 - Getting distribution'
dist = () # (n_argets, file_count)
# for example, (n_tokens=50, file_count=10k) means there are 10k files that have 50 tokens 

with open(FILE,'r') as tokens:
	for line in tokens:
		left_side = line.split('@#@')[0]
		n_targets = left_side.split(',')[2+LEVEL_NUM-1]
		if n_targets in dist:
			dist[n_targets] = dist[n_targets] + 1
		else:
			dist[n_targets] = 1

print '2 - Creating sorted list of targets'
sorted_targets = map(int, dist.keys()) # return integer from string
sorted_targets = filter(lambda l: l>=FILTER_THRESHOLD,sorted_targets)
sorted_targets = sorted(sorted_targets, key=int)

start_index = sorted_targets[0]
temp = dist[str(sorted_targets[0])]
stop_index = sorted_targets[0]
n_intervals = 0
copy_paste_result = []

print '3 - Finding Intervals'
# define intervals based on RULE #1 first, and then RULE #2
for a in sorted_targets[1:]:
	if temp + dist[str(a)] >= INTERVAL_SIZE:
		print start_index,'-',stop_index,'(',temp,'files',')'
		copy_paste_result += [stop_index]
		start_index = a
		stop_index = a
		temp = 0
		n_intervals +=1
	else:
		temp += dist[str(a)]
		stop_index = a
print start_index,'-',sorted_targets[-1],'(',temp,'files',')'
copy_paste_result += [stop_index]
n_intervals += 1
print 'Number of intervals:',n_intervals
print '** COPY-PASTE LINE BELOW into sourcerercc.properties **'
print 'SHARD_MAX_NUM_TOKENS='+','.join([str(i) for i in copy_paste_result])