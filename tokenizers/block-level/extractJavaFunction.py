import sys, os
import javalang
import logging

def getFunctions(filestring, logging, file_path):
  logging.warning("Starting block-level parsing on " + file_path)

  method_string = []
  method_pos    = []
  
  tree = None
  try:
    tree = javalang.parse.parse( filestring )
  except Exception as e:
    logging.warning("File " + file_path + " cannot be parsed. " + str(e))
    return (None, None)
  
  file_string_split = filestring.split('\n')
  
  for path, node in tree.filter(javalang.tree.MethodDeclaration):
    #print '### path',path
    #print '### node',node
    (init_line,b) = node.position
  
    method_body = []
    closed = 0
    openned = 0
    for line in file_string_split[init_line-1:]:
      closed  += line.count('}')
      openned += line.count('{')
      if (closed - openned) == 0:
        method_body.append(line)
        break
      else:
        method_body.append(line)
  
    end_line = init_line + len(method_body) - 1
    method_body = '\n'.join(method_body)
  
    method_pos.append((init_line,end_line))
    method_string.append(method_body)
  
  if (len(method_pos) == 0) or (len(method_string) == 0):
    return (None,None)
  else:
    return (method_pos,method_string)


if __name__ == "__main__":
  # Read straight from a file, for testing purposes

  FORMAT = '[%(levelname)s] (%(threadName)s) %(message)s'
  logging.basicConfig(level=logging.DEBUG,format=FORMAT)

  f = open(sys.argv[1],'r')
  filestring = f.read()

  (positions,strings) = getFunctions(filestring,sys.argv[1],logging)

  i = 0
  for elem in positions:
    print '#### method at:',elem
    print strings[i]
    print '---------------------------------------------------------'
    i += 1
