import sys
import os
import logging
import datetime
import MySQLdb
import time
import ConfigParser

sys.path.append(os.path.abspath("tokenizer"))
from tokenizerController import TokenizerController

sys.path.append(os.path.abspath("../clone-detector"))
from controller import ScriptController

if __name__ == '__main__':
    print 'Starting to process list ',sys.argv[1],'...'
    tokenizerController = TokenizerController(sys.argv[1])
    print 'Tokenizing...'
    #tokenizerController.execute()
    print 'Creating input for CC...'
    #tokenizerController.move_input_to_CC()

    print 'Running CC...'
    #params = {"num_nodes_search": 8}
    #controller = ScriptController(params)
    #controller.execute()

    print 'Importing CC output to the DB...'
    tokenizerController.import_pairs_to_DB()

    print 'Calculating project-level info...'
    #here

    print 'Finished'

