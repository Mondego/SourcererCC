#This script is configured for the Leidos environment
#Receives a list of GITHUB project paths, and returns a csv with
#  project_path,isFork,language,url,branch

#Usage: $python this-script.py list-of-github-projects.txt > projects-with-github-meta.txt


import sys
import os
import json

def get_github_info(path):
  try:
#               print 'Opening:',path+'/github'+'/info.json'
    with open(path+'/github'+'/info.json') as file1:
      data1 = json.load(file1)
    return [str(data1.get('fork', 'ERROR')),str(data1.get('languageMain', 'ERROR')),str(data1.get('html_url', 'ERROR')),str(data1.get('default_branch', 'ERROR'))]
  except:
    return []

def good_meta(meta):
#       print meta1
#       print meta2
  return (len(meta) == 4) and ('ERROR' not in ''.join(meta))

with open(sys.argv[1],'r') as file:
  for line in file:
    path = line.split(',')[0]
    meta = get_github_info(path)
    if good_meta(meta):
      git_url = meta[2]+'/tree/'+meta[3]
      result = ','.join([line[:-1],meta[0],meta[1],git_url])
      print result
    else:
      result = ','.join([line[:-1],'ERROR','ERROR','ERROR','ERROR'])
      print result

