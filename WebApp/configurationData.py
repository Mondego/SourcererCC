from configparser import ConfigParser
import os

config = ConfigParser()

try:
   config.read(os.path.join(os.path.dirname(os.path.abspath(__file__)) , 'config.ini'))
except IOError:
   print ('ERROR - Config settings not found. Usage: $python this-script.py config-file.ini')
   sys.exit()

sourcerer_path = config.get("PATHS","SOURCERER_PATH")
project_list_path = config.get("PATHS","PROJECT_LIST_PATH")
result_pair_path = config.get("PATHS","RESULT_PAIR_PATH")
run_environment = config.get("ENVIRONMENT", "RUN_ENVIRONMENT")