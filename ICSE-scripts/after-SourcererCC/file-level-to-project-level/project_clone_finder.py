#!/usr/bin/env python

# takes file level clones, and tries to find project level clones
#
#
# Usage: ./project_clone_finder.py <path to file with file level clones> <directory where (pid, fid, path of file) grouping files are> <percentage match filter> <output file path>

import os, sys

def read_fileclone_file(path_to_fileclone):
  return [((line.strip().split(","))[1],(line.strip().split(","))[4]) for line in open(path_to_fileclone).readlines()]

def read_fid_pid_input(path_to_fid_pid_input):
  fid_to_pid_map = {}
  pid_file_count = {}
  for f in os.listdir(path_to_fid_pid_input):
    with open(os.path.join(path_to_fid_pid_input, f)) as pfile:
      pairs = [tuple(line.strip().split(",")) for line in pfile.readlines()]
      for i in range(len(pairs)):
        pid, fid = pairs[i][:2]
        pid_file_count[pid] = pid_file_count.setdefault(pid, 0) + 1
        if fid in fid_to_pid_map:
          print fid
        else:
          fid_to_pid_map[fid] = pid
  #print pid_file_count.items()[0]
  #print pid_file_count["126875"]
  return fid_to_pid_map, pid_file_count

def transform_clones(fileclones, fid_pid_map):
  pids_file_clones = {}
  i = 0
  count = len(fileclones)
  for fid1, fid2 in fileclones:
    pid1, pid2 = (fid_pid_map[fid1], fid_pid_map[fid2])
    pids_file_clones.setdefault((pid1, pid2), {}).setdefault(pid1, set()).add(fid1)
    pids_file_clones[(pid1, pid2)].setdefault(pid2, set()).add(fid2)
    i += 1
    if i%100000 == 0:
      print i, count

  group_count = {}
  i = 0
  count = len(pids_file_clones)
  for pid1, pid2 in pids_file_clones:
    group_count[(pid1, pid2)] = {
        pid1: len(pids_file_clones[(pid1, pid2)][pid1]),
        pid2: len(pids_file_clones[(pid1, pid2)][pid2])
    }
    i += 1
    if i%10000 == 0:
      print i, count
  return group_count

def find_clones(grouped_clones, pid_file_count):
  pid_clones = {}
  for  ((pid1, pid2), filecount) in grouped_clones.items():
    pid_clones.setdefault(pid1, {})[pid2] = filecount[pid1]
    pid_clones.setdefault(pid2, {})[pid1] = filecount[pid2]
#    if count > pid_file_count[pid1] or count > pid_file_count[pid2]:
#      print pid1, pid2, count, pid_file_count[pid1], pid_file_count[pid2]
#      sys.exit(0)
  pidper = []
  for pid1 in pid_clones:
    for pid2 in pid_clones[pid1]:
      pidper.append((pid1, pid2, pid_file_count[pid1], float(pid_clones[pid1][pid2]) * 100 / float(pid_file_count[pid1])))

  return sorted(
      pidper,
      key = lambda x: x[3],
      reverse = True)

def Find_and_Filter_Clones(path_to_fileclone, path_to_fid_pid_input, filter_percent):
  print "Reading file clone file"
  fileclones = read_fileclone_file(path_to_fileclone)
  print "Reading fid pid file"
  fid_pid_map, pid_file_count = read_fid_pid_input(path_to_fid_pid_input)
  print "grouping"
  grouped_clones = transform_clones(fileclones, fid_pid_map)
  print "finding clones"
  pid_clones = find_clones(grouped_clones, pid_file_count)
  print "filtering"
  return [item for item in pid_clones if item[3] >= float(filter_percent)]


if __name__ == "__main__":
  if len(sys.argv) != 5:
    print "The right usage is ./project_clone_finder.py <path to file with file level clones> <directory where (pid, fid, path of file) grouping files are> <percentage match filter> <output file path>"
    sys.exit(0)
  path_to_fileclone, path_to_fid_pid_input, filter_percent, outfile = sys.argv[1:]
  filtered_project_clones = Find_and_Filter_Clones(path_to_fileclone, path_to_fid_pid_input, filter_percent)
  open(outfile, "w").write("\n".join([",".join([pid1, pid2, str(count), "%.2f" % percent]) for pid1, pid2, count, percent in filtered_project_clones]))



