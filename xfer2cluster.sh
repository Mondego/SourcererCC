#!/bin/bash
echo "creating jar"
ant clean cdi
echo "xfering input folder"
starcluster put sd6 input /home
echo "xfering dist folder"
starcluster put sd6 dist /home/
echo "xfering properties file"
starcluster put sd6 sourcerer-cc.properties /home/
echo "xfering .sh files"
starcluster put sd6 *.sh /home/
echo "xfering finished"
