from configparser import ConfigParser
import os

config = ConfigParser()

try:
   config.read(os.path.join(os.path.dirname(os.path.abspath(__file__)) , 'config.ini'))
except IOError:
   print ('ERROR - Config settings not found. Usage: $python this-script.py config-file.ini')
   sys.exit()

web_path = os.path.dirname(os.path.abspath(__file__))
sourcerer_path = os.path.abspath(os.path.join(web_path, os.pardir, os.pardir))
project_list_path = os.path.join(sourcerer_path, "SourcererCC", config.get("PATHS","PROJECT_LIST_PATH"))
result_pair_path = os.path.join(sourcerer_path, "SourcererCC", config.get("PATHS","RESULT_PAIR_PATH"))
run_environment = config.get("ENVIRONMENT", "RUN_ENVIRONMENT")