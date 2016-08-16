# Script used for optimizing runs of SourcererCC. The number of shards will correspond to the
#   input sizes and to the number of times SourcererCC will have to run, so ajust
#   *interval_size* accordingly.
# The result of this script should be copy-paste into the file *sourcerer-cc-properties*
#   inside the running environmnt of SourcererCC

# Usage:
# $ python step2-find-sharding-intervals.py unique-hashes.txt
import sys

interval_size = 50000 #half a million

file = sys.argv[1]
filter_threshold = 65

res = dict()
print '1 - Building dict'

with open(file,'r') as tokens:
        for line in tokens:
                left_side = line.split('@#@')[0]
                n_unique_tokens = left_side.split(',')[2]
                if n_unique_tokens in res:
                        res[n_unique_tokens] = res[n_unique_tokens] + 1
                else:
                        res[n_unique_tokens] = 1

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
