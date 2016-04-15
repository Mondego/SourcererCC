#!/bin/bash
offset=0
num_nodes="${1:-100}"
mv completed_queries.txt completed_queries_$(date +"%Y%m%d_%H%M%S")
python completed.py rr > rr.log
python completed.py gw $num_nodes
ant clean cdi
num_nodes="${1:-100}"
for i in $(seq 1 1 $num_nodes)
#for i in `cat jmiss`
do
  node=$(($offset+$i))
  echo "qsub worker_$i.sh"
  qsub worker_$i.sh
done
echo "done"
