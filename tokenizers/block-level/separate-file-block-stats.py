import sys, os

for file in os.listdir("file_block_stats"):
	readfile = os.path.join("file_block_stats", file)
	with open(readfile, "r") as file_book:
		with open(os.path.join("blocks_stats", file.replace("file", "block")), 'w') as block_info:
			with open(os.path.join("files_stats", file), "w") as file_info:
				for line in file_book:
					if line.startswith('b'):
						block_info.write(line)
					elif line.startswith('f'):
						file_info.write(line)
					else:
						print "error", line
		
	print "Done with: ", readfile
