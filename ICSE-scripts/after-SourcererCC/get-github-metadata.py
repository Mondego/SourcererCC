# This script is used to transform a list of file clone pairs in their internal identifiers format
#   to a list of file clone pairs in their github url.
# This script is particular to our dataset and unlikely to be re-usable. It is added here
# only for clarity and transparency.

import sys
import json
import os

def get_github_info(path):
	try:
#		print 'Opening:',path+'/github'+'/info.json'
		with open(path+'/github'+'/info.json') as file1:
			data1 = json.load(file1)
		return [str(data1.get('fork', 'ERROR')),str(data1.get('language', 'ERROR')),str(data1.get('html_url', 'ERROR')),str(data1.get('default_branch', 'ERROR'))]
	except:
		return []

def good_metas(meta1,meta2):
	if ((len(meta1) == len(meta2) == 0)):
		return False
	if (('ERROR' in ''.join(meta1)) or ('ERROR' in ''.join(meta2))):
		return False
	return ((meta1[0] is 'False') and (meta2[0] is 'False') and (meta1[1] is 'Java') and (meta2[1] is 'Java'))

def get_file_url(path):
	return path[path.find('latest')+7:]

file_bookkeeping = dict()

#Creating a dict for performance
for file in os.listdir(sys.argv[1]):
		if file.endswith('.txt'):
			file = os.path.join(sys.argv[1],file)
			#print 'Searching on ',file
			with open(file,'r') as file_book:
				for line in file_book:
					line_split = line[:-1].split(',')
					file_bookkeeping[line_split[0]+','+line_split[1]] = line_split[2]

with open(sys.argv[2],'r') as clones:
	for line in clones:
		line_split = line[:-1].split(',')
		file1 = line_split[0]+','+line_split[1]
		file2 = line_split[2]+','+line_split[3]
		meta1 = get_github_info(file_bookkeeping[file1][0:70])
		meta2 = get_github_info(file_bookkeeping[file2][0:70])
		if good_metas(meta1,meta2):
			print meta1[2]+'/tree/'+meta1[3]+'/'+get_file_url(file_bookkeeping[file1])+','+ meta2[2]+'/tree/'+meta2[3]+'/'+get_file_url(file_bookkeeping[file2])


