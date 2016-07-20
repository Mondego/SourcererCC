#!/bin/bash
offset=0
start_id="${1:-1}"
stop_id="${2:-256}"
action="${3:-search}"
mv completed_queries.txt completed_queries_$(date +"%Y%m%d_%H%M%S")
if ["$action" = "search"]; then
	echo "action is search. taking backup of completed queries."
	python completed.py rr  > rr.log
fi
python completed.py gw $start_id $stop_id $action
ant clean cdi
for i in $(seq $start_id $stop_id)
#for i in `cat jmiss`
do
  echo "qsub worker_$i.sh"
  qsub worker_$i.sh
done
echo "done"
