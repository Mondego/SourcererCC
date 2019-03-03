# -*- coding: utf-8 -*-
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

    def __init__(self, params):
        self.params = {}
        self.params.update(params)
        self.script_meta_file_name = self.full_file_path("scriptinator_metadata.scc")
        self.current_state = ScriptController.STATE_EXECUTE_1  # default state
        self.previous_run_state = self.load_previous_state()

    # добавляет путь директории к string
    def full_file_path(self,string):
        return os.path.join(os.path.dirname(os.path.realpath(__file__)),string)

    # добавляет путь директории к string с параметрами param
    def full_script_path(self,string,param=""):
        if len(param) == 0:
            return os.path.join(os.path.dirname(os.path.realpath(__file__)),string)
        else:
            return os.path.join(os.path.dirname(os.path.realpath(__file__)),string)+" "+param

    # execute command
    # execute.sh 1 1&>Log_execute_1.out 2&>Log_execute_1.err
    #     restore-gtpm.sh 1&>Log_restore_gtpm.sh 2&>Log_restore_gtpm.err
    # or
    #     backup-gtpm.sh 1&>Log_backup_gtpm.sh 2&>Log_backup_gtpm.err
    # runnodes.sh init 1 1&>Log_init.out 2&>Log_init.err
    # runnodes.sh index 1 1&>Log_index.out 2&>Log_index.err
    # move-index.sh index 1 1&>Log_move_index.out 2&>Log_move_index.err
    # execute.sh {num_nodes_search} 1&>Log_execute_{num_nodes_search}.out 2&>Log_execute_{num_nodes_search}.err
    # runnodes.sh search {num_nodes_search} 1&>Log_search.out 2&>Log_search.err
    def execute(self):
        print("previous run state {s}".format(s=self.previous_run_state))
        if self.previous_run_state > ScriptController.STATE_EXECUTE_1:
            returncode = ScriptController.EXIT_SUCCESS
        else:
            command = self.full_script_path('execute.sh', "1")
            command_params = command.split()
            returncode = self.run_command(command_params, self.full_file_path("Log_execute_1.out"), self.full_file_path("Log_execute_1.err"))
        self.current_state += 1
        if returncode == ScriptController.EXIT_SUCCESS:
            self.flush_state()
            # execute the init command
            if self.previous_run_state > ScriptController.STATE_INIT:
                returncode = ScriptController.EXIT_SUCCESS
            else:
                if self.previous_run_state == ScriptController.STATE_INIT:
                    # last time the execution failed at init step. We need to replace the existing gtpm index  from the backup
                    command = self.full_script_path("restore-gtpm.sh")
                    command_params = command.split()
                    returncode = self.run_command(command_params, self.full_file_path("Log_restore_gtpm.out"), self.full_file_path("Log_restore_gtpm.err"))
                else:
                    # take backup of existing gtpmindex before starting init
                    command = self.full_script_path("backup-gtpm.sh")
                    command_params = command.split()
                    returncode = self.run_command(command_params, self.full_file_path("Log_backup_gtpm.out"), self.full_file_path("Log_backup_gtpm.err"))
                # run the init step
                command = self.full_script_path("runnodes.sh", "init 1")
                command_params = command.split()
                returncode = self.run_command(command_params, self.full_file_path("Log_init.out"), self.full_file_path("Log_init.err"))
            self.current_state += 1
            if returncode == ScriptController.EXIT_SUCCESS:
                self.flush_state()
                # execute index
                if self.previous_run_state > ScriptController.STATE_INDEX:
                    returncode = ScriptController.EXIT_SUCCESS
                else:
                    command = self.full_script_path("runnodes.sh", "index 1")
                    command_params = command.split()
                    returncode = self.run_command(
                        command_params, self.full_file_path("Log_index.out"), self.full_file_path("Log_index.err"))
                self.current_state += 1
                if returncode == ScriptController.EXIT_SUCCESS:
                    self.flush_state()
                    if self.previous_run_state > ScriptController.STATE_MOVE_INDEX:
                        returncode = ScriptController.EXIT_SUCCESS
                    else:
                        # execute move indexes
                        command = self.full_script_path("move-index.sh")
                        command_params = command.split()
                        returncode = self.run_command(command_params, self.full_file_path("Log_move_index.out"), self.full_file_path("Log_move_index.err"))
                    self.current_state += 1
                    if returncode == ScriptController.EXIT_SUCCESS:
                        self.flush_state()
                        if self.previous_run_state > ScriptController.STATE_EXECUTE_2:
                            returncode = ScriptController.EXIT_SUCCESS
                            # execute command to create the dir structure
                        else:
                            command = self.full_script_path("execute.sh", "{nodes}".format(nodes=self.params["num_nodes_search"]))
                            command_params = command.split()
                            returncode = self.run_command(command_params,
                                                          self.full_file_path("Log_execute_{nodes}.out".format(
                                                              nodes=self.params["num_nodes_search"])),
                                                          self.full_file_path("Log_execute_{nodes}.err".format(nodes=self.params["num_nodes_search"])))
                        self.current_state += 1
                        if returncode == ScriptController.EXIT_SUCCESS:
                            self.flush_state()
                            if self.previous_run_state > ScriptController.STATE_SEARCH:
                                returncode = ScriptController.EXIT_SUCCESS
                            else:
                                command = self.full_script_path("runnodes.sh", "search {nodes}".format(
                                    nodes=self.params["num_nodes_search"]))
                                command_params = command.split()
                                returncode = self.run_command(
                                    command_params, self.full_file_path("Log_search.out"), self.full_file_path("Log_search.err"))
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

    # флашит состояние в script_meta_file_name
    def flush_state(self):
        print("current state: ", str(self.current_state))
        with open(self.script_meta_file_name, "w") as f:
            print ("flushing current state", str(self.current_state))
            f.write("{line}\n".format(line=self.current_state))

    # загружает состояние предыдущего запуска
    def load_previous_state(self):
        print("loading previous run state")
        if os.path.isfile(self.script_meta_file_name):
            with open(self.script_meta_file_name, "r") as f:
                return int(f.readline())
        else:
            print("{f} doesn't exist, creating one with state EXECUTE_1".format(f=self.script_meta_file_name))
            return ScriptController.STATE_EXECUTE_1

    # запускает команду cmd и перенаправляет STDOUT в outFile и STDERR в errFile
    def run_command(self, cmd, outFile, errFile):
        print("running new command {}".format(" ".join(cmd)))
        with open(outFile, "w") as fo, open(errFile, "w") as fe:
            p = subprocess.Popen(cmd, stdout=fo, stderr=fe, universal_newlines=True)
            p.communicate()
        return p.returncode

if __name__ == '__main__':
    numnodes = 2
    if len(sys.argv) > 1:
        numnodes = int(sys.argv[1])
    print("search will be carried out with {num} nodes".format(num=numnodes))
    params = {"num_nodes_search": numnodes}

    controller = ScriptController(params)
    controller.execute()
