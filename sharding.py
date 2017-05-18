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
SIMILARITY = sys.argv[2]
INTERVAL_SIZE = sys.argv[3]
LEVEL_NUM = sys.argv[4]
FILTER_THRESHOLD = argv[5]


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


print '3 - Finding intervals'
# the lower bound for the first interval will be sorted_targets[0]
# it's either the FILTER_THRESOLD, or the minimum n_targets
start_index = sorted_targets[0]
copy_paste_result = [start_index]

temp = dist[str(sorted_targets[0])]
n_intervals = 0

# find the first interval (second stop_index) first
i = 1 # i marks the position in sorted_targets list
while temp + dist[str(sorted_targets[i])] < INTERVAL_SIZE:
	temp += dist[str(sorted_targets[i])]
	i += 1
# the first interval is decided by INTERVAL_SIZE (N_files >= INTERVAL_SIZE)
temp = 0
stop_index = sorted_targets[i]
n_intervals += 1
print start_index,'-',stop_index,'(',temp,'files',')'
copy_paste_result += [stop_index]

# define the rest of intervals based on RULE #1 first, and then RULE #2
while i < len(sorted_targets):
	start_index = stop_index
	# RULE #1
	min_stop_index = start_index/SIMILARITY*100
	# RULE #2
	# check if the number of files exceeds INTERVAL_SIZE
	while sorted_targets[i] <= min_stop_index:
		temp += dist[str(sorted_targets[i])]
		i += 1

	if temp >= INTERVAL_SIZE:
		stop_index = sorted_targets[i]
		copy_paste_result += [stop_index]
	else:
		while temp + dist[str(sorted_targets[i])] < INTERVAL_SIZE:
			temp += dist[str(sorted_targets[i])]
			i += 1
		stop_index = sorted_targets[i]
		copy_paste_result += [stop_index]

	print start_index,'-',stop_index,'(',temp,'files',')'
	temp = 0
	n_intervals += 1



print 'Number of intervals:',n_intervals
print '** COPY-PASTE LINE BELOW into sourcerercc.properties **'
print 'SHARD_MAX_NUM_TOKENS='+','.join([str(i) for i in copy_paste_result])