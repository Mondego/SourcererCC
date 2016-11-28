'''
Created on Nov 8, 2016

@author: saini
'''
from pathlib import Path
import subprocess
import sys


class ScriptControllerException(Exception):
    pass


class ScriptController(object):
    '''
    Aim of this class is to run the scripts for SourcererCC with a single command
    '''
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

    def __init__(self, params):
        self.params = {}
        self.params.update(params)
        self.script_meta_file_name = "scriptinator_metadata.scc"
        self.scriptinator_meta_file = Path(self.script_meta_file_name)
        self.current_state = ScriptController.STATE_EXECUTE_1  # default state
        self.previous_run_state = self.load_previous_state()

    def execute(self):
        # execute command
        if self.previous_run_state > ScriptController.STATE_EXECUTE_1:
            returncode = ScriptController.EXIT_SUCCESS
        else:
            command = "./execute.sh 1"
            command_params = command.split()
            returncode = self.run_command(
                command_params, "Log_execute_1.out", "Log_execute_1.err")
        self.current_state += 1
        if returncode == ScriptController.EXIT_SUCCESS:
            self.flush_state()
            # execute the init command
            if self.previous_run_state > ScriptController.STATE_INIT:
                returncode = ScriptController.EXIT_SUCCESS
            else:
                command = "./runnodes.sh init 1"
                command_params = command.split()
                returncode = self.run_command(
                    command_params, "Log_init.out", "Log_init.err")
            self.current_state += 1
            if returncode == ScriptController.EXIT_SUCCESS:
                self.flush_state()
                # execute index
                if self.previous_run_state > ScriptController.STATE_INDEX:
                    returncode = ScriptController.EXIT_SUCCESS
                else:
                    command = "./runnodes.sh index 1"
                    command_params = command.split()
                    returncode = self.run_command(
                        command_params, "Log_index.out", "Log_index.err")
                self.current_state += 1
                if returncode == ScriptController.EXIT_SUCCESS:
                    self.flush_state()
                    if self.previous_run_state > ScriptController.STATE_MOVE_INDEX:
                        returncode = ScriptController.EXIT_SUCCESS
                    else:
                        # execute move indexes
                        command = "./move-index.sh"
                        command_params = command.split()
                        returncode = self.run_command(
                            command_params, "Log_move_index.out", "Log_move_index.err")
                    self.current_state += 1
                    if returncode == ScriptController.EXIT_SUCCESS:
                        self.flush_state()
                        if self.previous_run_state > ScriptController.STATE_EXECUTE_2:
                            returncode = ScriptController.EXIT_SUCCESS
                            # execute command to create the dir structure
                            command = "./execute.sh {nodes}".format(
                                nodes=self.params["num_nodes_search"])
                            command_params = command.split()
                            returncode = self.run_command(command_params,
                                                          "Log_execute_{nodes}.out".format(
                                                              nodes=self.params["num_nodes_search"]),
                                                          "Log_execute_{nodes}.err".format(nodes=self.params["num_nodes_search"]))
                        self.current_state += 1
                        if returncode == ScriptController.EXIT_SUCCESS:
                            self.flush_state()
                            if self.previous_run_state > ScriptController.STATE_SEARCH:
                                returncode = ScriptController.EXIT_SUCCESS
                            else:
                                command = "./runnodes.sh search {nodes}".format(
                                    nodes=self.params["num_nodes_search"])
                                command_params = command.split()
                                returncode = self.run_command(
                                    command_params, "Log_search.out", "Log_search.err")
                            self.current_state = ScriptController.STATE_EXECUTE_1 # go back to EXE 1 state
                            if returncode == ScriptController.EXIT_SUCCESS:
                                self.flush_state()
                                print("SUCCESS: Search Completed on all nodes")
                            else:
                                raise ScriptControllerException("One or more nodes failed during Step Search. \
                                    Check Log_search.log for more details. grep for FAILED in the log file")
                        else:
                            raise ScriptControllerException(
                                "error in execute.sh script while preparing for the search step.")
                    else:
                        raise ScriptControllerException(
                            "error in move-index.sh script.")

                else:
                    raise ScriptControllerException("error during indexing.")
            else:
                raise ScriptControllerException("error during init.")
        else:
            raise ScriptControllerException(
                "error in execute.sh script while preparing for init step.")

    def flush_state(self):
        if self.scriptinator_meta_file.is_file():
            with open(self.script_meta_file_name, "w") as f:
                f.writeline(self.current_state)

    def load_previous_state(self):
        if self.scriptinator_meta_file.is_file():
            with open(self.script_meta_file_name, "r") as f:
                self.current_state = int(f.readline())
        else:
            self.current_state = STATE_EXECUTE_1

    def run_command(self, cmd, outFile, errFile):
        print("running new command {}".format(" ".join(cmd)))
        fo = open(outFile, "a")
        fe = open(errFile, "a")
        p = subprocess.Popen(cmd,
                             stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE,
                             universal_newlines=True
                             )
        while (True):
            returncode = p.poll()  # returns None while subprocess is running
            outputLine = p.stdout.readline()
            errLine = p.stderr.readline()
            if outputLine:
                fo.write(outputLine)
            if errLine:
                fe.write(errLine)
            if returncode is not None:
                print ("returncode is {}. ".format(returncode))
                break
        # read the remaining lines in the buffers
        outputLines = p.stdout.readlines()
        for outputLine in outputLines:
            fo.write(outputLine)
        errLines = p.stderr.readlines()
        for errLine in errLines:
            fo.write(errLine)
        fo.close()
        fe.close()
        return returncode
if __name__ == '__main__':
    numnodes = 2
    if len(sys.argv) > 1:
        numnodes = int(sys.argv[1])
    print("search will be carried out with {num} nodes".format(num=numnodes))
    params = {"num_nodes_search": numnodes}

    controller = ScriptController(params)
    controller.execute()
