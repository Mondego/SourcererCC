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
    print '__main__ input',sys.argv[1]
    tokenizerController = TokenizerController(sys.argv[1])
    tokenizerController.move_input_to_CC()

    params = {"num_nodes_search": 4}
    controller = ScriptController(params)
    controller.execute()
