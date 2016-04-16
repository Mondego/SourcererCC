#!/bin/bash
offset=0
start_id="${1:-1}"
stop_id="${2:-256}"

mv completed_queries.txt completed_queries_$(date +"%Y%m%d_%H%M%S")
python completed.py rr > rr.log
python completed.py gw $start_id $stop_id
ant clean cdi
for i in $(seq $start_id $stop_id)
#for i in `cat jmiss`
do
  echo "qsub worker_$i.sh"
  qsub worker_$i.sh
done
echo "done"
