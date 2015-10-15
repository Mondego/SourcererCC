#!/bin/bash
cluster_name="${1:-sdx}"
echo "cluster is $cluster_name"
echo "creating jar"
ant clean cdi
echo "xfering input folder"
starcluster put $cluster_name input /home
echo "xfering dist folder"
starcluster put $cluster_name dist /home/
echo "xfering properties file"
starcluster put $cluster_name sourcerer-cc.properties /home/
echo "xfering .sh files"
starcluster put $cluster_name *.sh /home/
echo "xfering finished"
