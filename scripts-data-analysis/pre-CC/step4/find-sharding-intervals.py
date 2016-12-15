#Given a list of tokenized files, produces shards whose threshold is a certain nuymber
# of files, defined below by [interval_size]. [filter_threshold] is the same small-file
# filter defined for CC.

#Usage: $python this-script.py list-of-tokenixed-files.tokens

import sys

file = sys.argv[1]
interval_size = 500000
filter_threshold = 65

res = dict()
print '1 - Building dict'

with open(file,'r') as tokens:
	for line in tokens:
		left_side = line.split('@#@')[0]
		n_tokens = left_side.split(',')[2]
		if n_tokens in res:
			res[n_tokens] = res[n_tokens] + 1
		else:
			res[n_tokens] = 1

print '2 - Creating Sorted list of unique tokens'
list_uniques = map(int,res.keys())
list_uniques = filter(lambda l: l>=filter_threshold,list_uniques)
list_uniques = sorted(list_uniques, key=int)

temp = res[str(list_uniques[0])]
start_index = list_uniques[0]
stop_index = list_uniques[0]
n_intervals = 0
copy_paste_result = []

print '3 - Finding Intervals'
for a in list_uniques[1:]:
	if temp + res[str(a)] >= interval_size:
		print start_index,'-',stop_index,'(',temp,'files',')'
		copy_paste_result += [stop_index]
		start_index = a
		stop_index = a
		temp = 0
		n_intervals +=1
	else:
		temp += res[str(a)]
		stop_index = a
print start_index,'-',list_uniques[-1],'(',temp,'files',')'
copy_paste_result += [stop_index]
n_intervals += 1
print 'Number of intervals:',n_intervals
print '** COPY-PASTE LINE BELOW into sourcerercc.properties **'
print 'SHARD_MAX_NUM_TOKENS='+','.join([str(i) for i in copy_paste_result])


