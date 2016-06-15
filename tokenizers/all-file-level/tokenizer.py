import sys
import subprocess

arg = ''
if len(sys.argv) > 1:
	arg = sys.argv[1]

functions = ['folder', 'tar', 'mirror']

if arg == functions[0]:
	subprocess.call("python src/tokenizer-directory.py", shell=True)
elif arg == functions[1]:
	subprocess.call("python src/tokenizer-tar.py", shell=True)
elif arg == functions[2]:
	subprocess.call("python src/tokens-mirror.py", shell=True)
else:
	# print 'No argument specified'
	print '\nPossible arguments are:\t' + ', '.join(functions) + '\n'
	sys.exit()
