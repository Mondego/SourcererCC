#!/bin/bash
shard=`python completed.py job`
echo "xfering.."
echo "shard id is $shard"
result=result2
mkdir -p ../$result/$shard/output
mkdir -p ../$result/$shard/logs
echo "getting list of completed nodes"
python completed.py lc
echo "received list of completed nodes"

echo "copying output files"
for node in `cat nodes.txt`
do
  mkdir -p ../$result/$shard/output/$node
  cp $node/output8.0/queryclones_index_WITH_FILTER.txt ../$result/$shard/output/$node/
done
echo "copying logs"
cp search.* ../$result/$shard/logs/
echo "transfering output and logs to amazon machine"
scp -r ../$result sourcerer@amazon.ics.uci.edu:/home/sourcerer/hades/clonedetection/$results/

echo "xfering finished"
