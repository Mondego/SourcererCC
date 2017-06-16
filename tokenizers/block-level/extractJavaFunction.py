import sys, os
import javalang
import logging
import traceback
import traceback

def getFunctions(filestring, logging, file_path, separators):
  logging.info("Starting block-level parsing on " + file_path)

  method_string = []
  method_pos    = []
  new_experimental_values = []
  tree = None
  try:
    tree = javalang.parse.parse( filestring )
  except Exception as e:
    logging.warning("File " + file_path + " cannot be parsed. (1)" + str(e))
    loggint.warning('Traceback:' + traceback.print_exc())
    return (None, None, [])

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

    ## CODE BELOW is for experimental tokenization for meta CC
    ## Set experimental = False to remove component
    experimental = True
    if experimental:
      separators_count  = 0
      assignments_count = 0 #(=)
      statements_count = 0
      expressions_count = 0
      m_tree = None
  
      try:
        m_tree = javalang.parser.Parser(javalang.tokenizer.tokenize(method_body)).parse_member_declaration()

        for path, node in m_tree.filter(javalang.tree.Statement):
          statements_count += 1

        for path, node in m_tree.filter(javalang.tree.Expression):
          expressions_count += 1

        for line in method_body.split('\n'):
          if ('=' in line) and ('==' not in line):
            assignments_count += 1

        for x in separators:
          separators_count += method_body.count(x)

        aux = '%s,%s,%s,%s' % (separators_count,assignments_count,statements_count,expressions_count) # String must go formatted to files_tokens
        new_experimental_values.append(aux)
      except Exception as e:
        logging.warning("File " + file_path + " cannot be parsed. (2)" + str(e))
        loggint.warning('Traceback:' + traceback.print_exc())
        return (None,None,[])

  if (len(method_pos) != len(method_string)):
    logging.warning("File " + file_path + " cannot be parsed. (3)")
    return (None,None,new_experimental_values)
  else:
    logging.warning("File " + file_path + " successfully parsed.")
    return (method_pos,method_string,new_experimental_values)


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
