from __future__ import absolute_import, division, print_function, unicode_literals
import subprocess
import sys
import os

class ScriptControllerException(Exception):
    pass

# Aim of this class is to run the scripts for SourcererCC with a single command
class ScriptController(object):
    # exit codes
    EXIT_SUCCESS = 0
    EXIT_FAILURE = 1
    # states
    STATE_EXECUTE_1 = 0
    STATE_INIT = 1
    STATE_INDEX = 2
    STATE_MOVE_INDEX = 3
    STATE_EXECUTE_2 = 4
    STATE_SEARCH = 5

    def __init__(self, num_nodes):
        self.num_nodes_search = num_nodes
        self.script_meta_file_name = self.full_file_path("scriptinator_metadata.scc")
        self.current_state = ScriptController.STATE_EXECUTE_1  # default state
        self.previous_run_state = self.load_previous_state()

    def full_file_path(self,string):
        return os.path.join(os.path.dirname(os.path.realpath(__file__)), string)

    def full_script_path(self,string,param = ""):
        res = self.full_file_path(string)
        if param != "":
            res += " " + param
        return res

    def execute(self):
        # execute command
        print("previous run state {}".format(self.previous_run_state))
        if self.previous_run_state > ScriptController.STATE_EXECUTE_1:
            returncode = ScriptController.EXIT_SUCCESS
        else:
            returncode = self.run_command_wrapper("execute.sh", "1", "execute_1")
        self.current_state += 1
        if returncode == ScriptController.EXIT_SUCCESS:
            self.flush_state()
            # execute the init command
            if self.previous_run_state > ScriptController.STATE_INIT:
                returncode = ScriptController.EXIT_SUCCESS
            else:
                if self.previous_run_state == ScriptController.STATE_INIT:
                    # last time the execution failed at init step. We need to replace the existing gtpm index  from the backup
                    returncode = self.run_command_wrapper("restore-gtpm.sh", "", "restore_gtpm")
                else:
                    # take backup of existing gtpmindex before starting init
                    returncode = self.run_command_wrapper("backup-gtpm.sh", "", "backup_gtpm")
                # run the init step
                returncode = self.run_command_wrapper("runnodes.sh", "init 1", "init")
            self.current_state += 1
            if returncode == ScriptController.EXIT_SUCCESS:
                # execute index
                returncode = self.perform_step(ScriptController.STATE_INDEX, "runnodes.sh", "index 1", "index")
                if returncode == ScriptController.EXIT_SUCCESS:
                    # execute move indexes
                    returncode = self.perform_step(ScriptController.STATE_MOVE_INDEX, "move-index.sh", "", "move_index")
                    if returncode == ScriptController.EXIT_SUCCESS:
                        # execute command to create the dir structure
                        returncode = self.perform_step(ScriptController.STATE_EXECUTE_2, "execute.sh", "{}".format(self.num_nodes_search), "execute_{}".format(self.num_nodes_search))
                        if returncode == ScriptController.EXIT_SUCCESS:
                            returncode = self.perform_step(ScriptController.STATE_SEARCH, "runnodes.sh", "search {}".format(self.num_nodes_search), "search_{}".format(self.num_nodes_search))
                            self.current_state = ScriptController.STATE_EXECUTE_1 # go back to EXE 1 state
                            if returncode == ScriptController.EXIT_SUCCESS:
                                self.flush_state()
                                print("SUCCESS: Search Completed on all nodes")
                            else:
                                raise ScriptControllerException("One or more nodes failed during Step Search. \
                                    Check Log_search.log for more details. grep for FAILED in the log file")
                        else:
                            raise ScriptControllerException("error in execute.sh script while preparing for the search step.")
                    else:
                        raise ScriptControllerException("error in move-index.sh script.")
                else:
                    raise ScriptControllerException("error during indexing.")
            else:
                raise ScriptControllerException("error during init.")
        else:
            raise ScriptControllerException("error in execute.sh script while preparing for init step.")

    def perform_step(self, state, cmd, params, cmd_shortcut):
        returncode = ScriptController.EXIT_SUCCESS
        self.flush_state()
        if self.previous_run_state <= state:
            returncode = self.run_command_wrapper(cmd, params, cmd_shortcut)
        self.current_state += 1
        return returncode

    def flush_state(self):
        print("current state: {}".format(self.current_state))
        with open(self.script_meta_file_name, "w") as f:
            print ("flushing current state {}".format(self.current_state))
            f.write("{}\n".format(self.current_state))

    def load_previous_state(self):
        print("loading previous run state")
        if os.path.isfile(self.script_meta_file_name):
            with open(self.script_meta_file_name, "r") as f:
                return int(f.readline())
        else:
            print("{} doesn't exist, creating one with state EXECUTE_1".format(self.script_meta_file_name))
            return ScriptController.STATE_EXECUTE_1
    
    def run_command_wrapper(self, cmd, params, cmd_shortcut):
        command = self.full_script_path(cmd, params)
        out_file = self.full_file_path("Log_{}.out".format(cmd_shortcut))
        err_file = self.full_file_path("Log_{}.err".format(cmd_shortcut))
        return self.run_command(command.split(), out_file, err_file)

    def run_command(self, cmd, outFile, errFile):
        print("running new command {}".format(" ".join(cmd)))
        with open(outFile, "w") as fo, open(errFile, "w") as fe:
            p = subprocess.Popen(cmd, stdout = fo, stderr = fe, universal_newlines = True)
            p.communicate()
        return p.returncode

if __name__ == '__main__':
    numnodes = 2
    if len(sys.argv) >= 2:
        numnodes = int(sys.argv[1])
    print("search will be carried out with {} nodes".format(numnodes))

    controller = ScriptController(numnodes)
    controller.execute()
