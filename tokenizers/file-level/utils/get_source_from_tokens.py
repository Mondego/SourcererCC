import datetime as dt
import time
from optparse import OptionParser
import sys, os
import zipfile

default_output_folder = 'projects_from_blocks'

def grab_ids(folder_or_file):
  paths = set()
  if os.path.isdir(folder_or_file):
    for file in os.listdir(folder_or_file):
      if file.endswith(".tokens"):
        paths.add(os.path.join(folder_or_file, file))
  else:
    if os.path.isfile(folder_or_file):
      paths.add(folder_or_file)
    else:
      print "ERROR: '",projects_from_blocks,"' not found!"
  
  res = set()
  for p in paths:
    with open(p,'r') as file:
      for line in file:
        res.add(line.split(',')[1])

  return res

def copy_files(ids_set, folder_or_file, output_folder):
  copy_count = 0
  paths = set()

  if os.path.isdir(folder_or_file):
    for file in os.listdir(folder_or_file):
      if file.endswith(".stats"):
        paths.add(os.path.join(folder_or_file, file))
  else:
    if os.path.isfile(folder_or_file):
      paths.add(folder_or_file)
    else:
      print "ERROR: '",projects_from_blocks,"' not found!"

  for p in paths:
    with open(p,'r') as file:
      for line in file:
        line_split = line.split(',')
        if line.split(',')[1] in ids_set:
          # Next split is complicated to cut the string by quotation marks
          # (can't use only the commas because some paths do have them)
          full_path = line.split('","')[0].split(',"')[1] 
          zip_path  = full_path[:full_path.find('.zip')+4]
          file_path = full_path[full_path.find('.zip')+5:]

          if not os.path.isdir(os.path.dirname(os.path.join(output_folder,file_path))):
            os.makedirs(os.path.dirname(os.path.join(output_folder,file_path)))

          try:
            with zipfile.ZipFile(zip_path) as z:
              with open(os.path.join(output_folder,file_path), 'w') as f:
                f.write(z.read(file_path))
          except Exception as e:
            print 'ERROR reading',zip_path,e


          copy_count += 1

  return copy_count

if __name__ == "__main__":

  parser = OptionParser()
  parser.add_option("-b", "--tokensFiles", dest="tokensFiles", type="string", default=False,
                    help="File or folder with tokens files (*.tokens).")

  parser.add_option("-s", "--statsFiles", dest="statsFiles", type="string", default=False,
                    help="File or folder with stats files (*.stats).")

  parser.add_option("-o", "--output", dest="outputDir", type="string", default=False,
                    help="[OPTIONAL] Output folder for the files.")

  (options, args) = parser.parse_args()

  if not len(sys.argv) > 1:
    print "No arguments were passed. Try running with '--help'."
    sys.exit(0)

  if (not options.tokensFiles) or (not options.statsFiles):
    print "Arguments '-b' and '-s' are mandatory. Try running with '--help'."
    sys.exit(0)

  #### ARGUMENTS HANDLING MUST BE below
  output_folder = default_output_folder
  if options.outputDir:
    if os.path.isdir(options.outputDir):
      print 'Folder',options.outputDir,'already exists.'
      sys.exit(0)
    else:
      os.makedirs(options.outputDir)
      output_folder = options.outputDir
      print 'Folder',options.outputDir,'created.'
  else:
    if os.path.isdir(default_output_folder):
      print 'Folder',default_output_folder,'already exists.'
      sys.exit(0)
    else:
      os.makedirs(default_output_folder)
      print 'Folder',default_output_folder,'created.'

  p_start = dt.datetime.now()
  print 'Grabbing IDs...'
  ids_set = set()
  ids_set = grab_ids(options.tokensFiles)
  print '%s file ids in %s' % (len(ids_set), dt.datetime.now() - p_start)

  p_start = dt.datetime.now()
  print 'Copying files...'
  copy_count = copy_files(ids_set, options.statsFiles, default_output_folder)
  print '%s files copied in %s' % (copy_count, dt.datetime.now() - p_start)


