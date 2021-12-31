'''
Created on Dec 22, 2016

@author: vaibhavsaini
'''
import sys
import math

class Spliter(object):
    '''
    classdocs
    '''


    def __init__(self, params):
        '''
        Constructor
        '''
        self.split_count= params['split_count']
        self.input_filename = params['input_filename']
        self.total_lines = self.get_num_lines_in_input_file()
        self.find_base_x()
    
    def split(self):
        """
        splits the input file into split_count number of files. 
        """
        count=0
        line_limit = self.base_x
        print("line_limit is ", line_limit )
        file_count=1
        try:
            print("creating split ",file_count)
            self.outfile = open("query_{part}.file".format(part=file_count),'w')
            with open(self.input_filename,'r') as inputfile:
                for row in inputfile:
                    if count<line_limit:
                        self.outfile.write(row)
                    else:
                        self.outfile.flush()
                        self.outfile.close()
                        file_count+=1
                        count =0
                        line_limit =line_limit + math.ceil(0.5*self.base_x)
                        print("line_limit is ", line_limit )
                        print("creating split ",file_count )
                        self.outfile = open("query_{part}.file".format(part=file_count),'w')
                        self.outfile.write(row)
                    count+=1
            self.outfile.flush()
            self.outfile.close()
        except IOError as e:
            print("Error: {error}".format(error=e))
            sys.exit(1)
                
    
    def get_num_lines_in_input_file(self):
        with open(self.input_filename) as f:
            for i, l in enumerate(f):
                pass
        print("total lines in the inputfile: {0} ".format(i+1))
        return i + 1
    
    def find_base_x(self):
        # formula for S = x + x+.5x + x+2*.5x...x + (N-1)*.5x
        self.base_x= math.ceil(float(2*self.total_lines)/(float((self.split_count+1)*(self.split_count+2)/2) - 1))
        print("base_x is ", self.base_x)
        
if __name__ == '__main__':
    
    
    input_file = sys.argv[1]
    split_count = int(sys.argv[2])
    params= {'split_count':split_count,
             'input_filename' : input_file}
    print("spliting {inputfile} in {count} chunks".format(inputfile=input_file,count=split_count))
    splitter = Spliter(params)
    splitter.split()
    print("splitting done!")
