'''
Created on Dec 20, 2016

@author: vaibhavsaini
'''
import sys, os


class Analyzer(object):
    def __init__(self, inputfile):
        self.clonesFilename = inputfile
        self.distinct_pairs = set()
        self.clone_groups = {}

    def populate_distinct_clone_groups_count(self):
        count = 0
        print_per_k = 500000
        with open(self.clonesFilename, 'r') as clonesFile:
            for row in clonesFile:
                row = row[:-1]  # remove newline character
                row_as_array = row.split(',')
                lhsFile = ','.join([row_as_array[0], row_as_array[1]])  # project_id, file_id from lhs
                rhsFile = ','.join([row_as_array[2], row_as_array[3]])  # project_id, file_id from rhs
                if lhsFile in self.clone_groups:
                    self.clone_groups[lhsFile] += 1
                else:
                    self.clone_groups[lhsFile] = 1
                
                if rhsFile in self.clone_groups:
                    self.clone_groups[rhsFile] += 1
                else:
                    self.clone_groups[rhsFile] = 1
                count += 1
                if (count % print_per_k) == 0:
                    print "rows processed: ", count
        print "rows processed: ", count

    def print_dict(self, dict_to_print):
        print("clones of each file:")
        with open("results.txt", 'w') as resultfile:
            for key, val in sorted(dict_to_print.items(), key=lambda x:-x[1]):
                resultfile.write("{key},{val}\n".format(key=key, val=val))
            
if __name__ == '__main__':
    pairs_file = sys.argv[1]
    analyzer = Analyzer(pairs_file)
    # analyzer.get_count_of_distinct_files_that_have_clones()
    analyzer.populate_distinct_clone_groups_count()
    analyzer.print_dict(analyzer.clone_groups)
    print "count of distinct files that have clones", len(analyzer.clone_groups.keys())
