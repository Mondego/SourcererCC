##
## Convert the output of SourcererCC to something more friendly
##
## Author: Crista Lopes
##

import sys, os
from os.path import join

class FileData:
    def __init__(self, _path, _sloc):
        self.path = _path
        self.sloc = _sloc

def load_name_map(file, name_map):
    with open(file) as f:
        for line in f:
            proj_id, file_id, gpath, lpath, hsh, size, ln, loc, sloc = line.strip().split(',')
            fid = proj_id + "#" + file_id
            if fid not in name_map:
                name_map[fid] = FileData(gpath, sloc)
            elif name_map[fid].path != gpath:
                print(f'Warning: file {fid} {gpath} already present in name map with global path {name_map[fid].path}')

def load_and_write_pairs(file, fout, name_map):
    with open(file) as f:
        for line in f:
            p1, f1, p2, f2 = line.strip().split(',')
            fid1 = p1 + "#" + f1
            fid2 = p2 + "#" + f2
            if fid1 not in name_map or fid2 not in name_map:
                print(f'Warning: either {fid1} or {fid2} not present in name map')
                fout.write(f'{fid1},{fid1},unknown,{fid2},{fid2},unknown\n')
            else:
                fout.write(f'{fid1},{name_map[fid1].path},{name_map[fid1].sloc},{fid2},{name_map[fid2].path},{name_map[fid2].sloc}\n')

if __name__ == '__main__':
    if len(sys.argv) > 1:
        datafolder = sys.argv[1]
    else:
        datafolder = '.'

name_map = {}
fcount = 0
for file in os.listdir(datafolder):
    if file.endswith('.stats'):
        load_name_map(join(datafolder, file), name_map)
        fcount += 1
if fcount == 0:
    print('Warning: no file stats files found! Nothing to do.')
    exit(0)

fcount = 0
with open('clones_with_names.csv', 'w') as fout:
    for file in os.listdir(datafolder):
        if file.endswith('.pairs'):
            load_and_write_pairs(join(datafolder, file), fout, name_map)
            fcount += 1
if fcount == 0:
    print('Warning: no clone pair files found! Nothing to do!')
else:
    print('Done. Output file is clones_with_names.csv')
