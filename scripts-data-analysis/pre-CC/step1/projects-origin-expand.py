#This script is configured for the Leidos environment
#Receives a list of project paths, and returns a csv with
#  project_path,origin

#Usage: $python this-script.py list-of-projects.txt > project-with-origin.txt

import sys
import os

def find_origin(path):
	if not os.path.isdir(path):
		return 'FOLDER-NOT-FOUND'
        if os.path.isdir(path+'/uci2010'):
                return 'uci2010'
        if os.path.isdir(path+'/uci2011'):
                return 'uci2011'
        if os.path.isdir(path+'/github'):
                return 'github'
        if os.path.isdir(path+'/java2s'):
                return 'java2s'
        if os.path.isdir(path+'/uciMaven'):
                return 'uciMaven'
        if os.path.isdir(path+'/fedora'):
                return 'fedora'
        if os.path.isdir(path+'/sourceforge'):
                return 'sourceforge'
        if os.path.isdir(path+'/google'):
                return 'google'
        return 'other'

with open(sys.argv[1],'r') as projects:
        for line in projects:
                p = line[:-1]
                print p+','+find_origin(p)





