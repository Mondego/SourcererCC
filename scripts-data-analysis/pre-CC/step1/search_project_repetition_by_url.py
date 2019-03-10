#Receives a list of github project paths, after expansion,
# and removes repeated projects (because some times different)
# project paths correspond to the same git url, and consequently
# to the same project

#Usage: $python this-script.py list-of-github-projects.txt > projects-without-repetition.txt


import sys

projects = set()

with open(sys.argv[1],'r') as file:
  for line in file:
    line_split = line[:-1].split(',')
    git_url = line_split[4]
    if git_url not in projects:
      projects.add(git_url)
      print line[:-1]


