import os
import re
import collections
import itertools

# Provided by the user
separators = [',',';','#','(',')','{','}','[',']','*','+','/','\\','-','&',':','<','>','=','"','\'','.','?','\n']
stop_words = ['return','void','int','ifndef','endif','class','private','float','>>','<<','+=','-=','define']
comment_end_of_line = '//'
comment_open_tag = re.escape('/*')
comment_close_tag = re.escape('*/')
path_with_files = 'examples/'


bookkeeping_file = open('bookkeeping.file','w')
blocks_file = open('blocks.file','w')

# Capture all files in path. Does NOT go recursively
onlyfiles = [f for f in os.listdir(path_with_files) if os.path.isfile(os.path.join(path_with_files, f))]

# This feels dumb, but unrolling a list of tuples is such a pain
bookkeeping = {}
aux = 0
for f in onlyfiles:
	bookkeeping[aux] = f
	aux +=1

for file_number, s_file in bookkeeping.items():
	print 'Processing file n '+str(file_number)+' ('+path_with_files+s_file+')'

	source_file = open(path_with_files+s_file,'r')

	with open(path_with_files+s_file,'r') as myfile:
		file_string = myfile.read()

	# Remove enf of line comments
	file_string = re.sub(comment_end_of_line+'.*?\n','',file_string,flags=re.DOTALL)
	# Remove tagged comments
	file_string = re.sub(comment_open_tag+'.*?'+comment_close_tag,'',file_string,flags=re.DOTALL)
	#Transform separators into spaces (remove them)
	file_string = ''.join(map(lambda x: '' if x in separators else x, file_string))
	#create list of words
	file_string = re.sub("[^\w]", " ",  file_string).split()
	#Remove stop words
	file_string = filter(lambda x: x not in stop_words, file_string)
	#Count occurrences
	file_string = collections.Counter(file_string)

	#Converting Counter to dict because according to StackOverflow is better
	file_string=dict(file_string)
	tokens = []
	#SourcererCC formatting
	for k, v in file_string.items():
		tokens.append(k+'@@::@@'+str(v))
	tokens = ','.join(tokens)

	bookkeeping_file.write('-1,'+str(file_number)+','+path_with_files+s_file+'\n')
	blocks_file.write('-1,'+path_with_files+s_file+'@#@'+tokens+'\n')
	
	bookkeeping_file.flush()
	blocks_file.flush()

	#print file_string

bookkeeping_file.close()
blocks_file.close()
