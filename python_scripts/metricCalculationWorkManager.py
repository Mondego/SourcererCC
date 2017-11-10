import os
import subprocess


def full_file_path(string):
    return os.path.join(os.path.dirname(os.path.realpath(__file__)), string)


def full_script_path(string, param=""):
    if len(param) == 0:
        return os.path.join(os.path.dirname(os.path.realpath(__file__)), string)
    else:
        return os.path.join(os.path.dirname(os.path.realpath(__file__)), string) + " " + param

def run_command(cmd, outFile, errFile):
    print("running new command {}".format(" ".join(cmd)))
    fo = open(outFile, "w")
    fe = open(errFile, "w")
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
            print("returncode is {}. ".format(returncode))
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
num_process = int(input("Enter the number of processes: "))

main_dir="../train_dataset"
subdirs = [os.path.basename(os.path.normpath(f.path)) for f in os.scandir(main_dir) if f.is_dir() ]
num_dir_per_process=len(subdirs)//num_process
num_last_file=num_dir_per_process+(len(subdirs)%num_process)

if not os.path.exists("output"):
    os.makedirs("output")

for i in range(num_process):
    file = open("output/file" + str(i + 1) + ".txt", "w")
    numWritten=0
    for j in range((i*num_dir_per_process),len(subdirs)):
        file.write(subdirs[j]+"\n")
        numWritten+=1
        if(numWritten==(num_dir_per_process) and i!=(num_process-1)):
            break
    file.close()
for file in os.listdir("output/"):
    command = " java -jar {filename}".format(
        filename=file)
    command_params = command.split()
    returncode = run_command(
        command_params, full_file_path("metric.out"), full_file_path("metric.err"))
