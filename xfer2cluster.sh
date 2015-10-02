#!/bin/bash
echo "creating jar"
ant clean cdi
echo "xfering input folder"
starcluster put sdx input /home
echo "xfering dist folder"
starcluster put sdx dist /home/
echo "xfering properties file"
starcluster put sdx sourcerer-cc.properties /home/
echo "xfering .sh files"
starcluster put sdx *.sh /home/
echo "xfering finished"
