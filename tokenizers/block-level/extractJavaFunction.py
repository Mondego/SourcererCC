import sys, os
import javalang
import logging
import traceback
import itertools

def getFunctions(filestring, logging, file_path, separators):
  logging.info("Starting block-level parsing on " + file_path)

  method_string = []
  method_pos    = []
  method_name = []
  tree = None

  try:
    tree = javalang.parse.parse( filestring )
    package = tree.package
    if package is None:
      package = 'JHawkDefaultPackage'
    else:
      package = package.name
  except Exception as e:
    logging.warning("File " + file_path + " cannot be parsed. (1)" + str(e))
    #logging.warning('Traceback:' + traceback.print_exc())
    return (None, None, [])

  file_string_split = filestring.split('\n')

  for path, node in tree.filter(javalang.tree.ClassDeclaration):
    package = package+"."+node.name

    inner_classes = dict()

    #Methods and Class constructors
    nodes = itertools.chain(node.filter(javalang.tree.ConstructorDeclaration), node.filter(javalang.tree.MethodDeclaration))

    for path_m,node_m in nodes:

      #Handling FQN's for inner classes
      parent_name = ''
      if isinstance(node_m,javalang.tree.MethodDeclaration):
        parent = path_m[:-1][-1]
        if isinstance(parent, javalang.tree.ClassCreator):
          if parent.type.name not in inner_classes:
            parent_name = '$'+parent.type.name
            inner_classes[parent.type.name] = 0
          else:
            parent_name = '$'+parent.type.name+'_'+str(inner_classes[parent.type.name])
            inner_classes[parent.type.name] = inner_classes[parent.type.name]+1

      (init_line,b) = node_m.position
      method_body = []
      closed = 0
      openned = 0

      args = []
      for t in node_m.parameters:
        dims = []
        if len(t.type.dimensions) > 0:
          for e in t.type.dimensions:
            dims.append("[]")
        dims = "".join(dims)
        args.append(t.type.name+dims)
      args = ",".join(args)

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

      method_name.append(("%s%s.%s(%s)") % (package,parent_name,node_m.name,args))

      print ("%s%s.%s(%s)") % (package,parent_name,node_m.name,args)

  if (len(method_pos) != len(method_string)):
    logging.warning("File " + file_path + " cannot be parsed. (3)")
    return (None,None,method_name)
  else:
    logging.warning("File " + file_path + " successfully parsed.")
    return (method_pos,method_string,method_name)


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
