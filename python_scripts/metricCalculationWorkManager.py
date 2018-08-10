import os
import subprocess
import shutil
import zipfile
import sys

def full_file_path(string):
    return os.path.join(os.path.dirname(os.path.realpath(__file__)), string)


def full_script_path(string, param=""):
    if len(param) == 0:
        return os.path.join(os.path.dirname(os.path.realpath(__file__)), string)
    else:
        return os.path.join(os.path.dirname(os.path.realpath(__file__)), string) + " " + param

def run_command(cmd, outFile, errFile):
    print("running new command {}".format(" ".join(cmd)))
    #fo = open(outFile, "w")
    #fe = open(errFile, "w")
    p = subprocess.Popen(cmd,
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE,
                         universal_newlines=True
                         )
   # while (True):
   #     returncode = p.poll()  # returns None while subprocess is running
   #     outputLine = p.stdout.readline()
   #     errLine = p.stderr.readline()
   #     if outputLine:
   #         fo.write(outputLine)
   #     if errLine:
   #         fe.write(errLine)
   #     if returncode is not None:
   #         print("returncode is {}. ".format(returncode))
   #         break
    # read the remaining lines in the buffers
    #outputLines = p.stdout.readlines()
    #for outputLine in outputLines:
    #    fo.write(outputLine)
    #errLines = p.stderr.readlines()
    #for errLine in errLines:
    #    fo.write(errLine)
    #fo.close()
    #fe.close()

if __name__ == '__main__':
    num_process = 2
    if len(sys.argv) > 2:
        num_process = int(sys.argv[1])
        type_input=sys.argv[2]
    else:
        print("Please provide all arguments")
if(type_input=='z' or type_input=='d'):

    main_dir="/scratch/mondego/local/farima/new_oreo/scalability_related/dataset/dataset/"
    # main_dir="D:\\PhD\\Clone\\MlCC-New\\SourcererCC\\test_input"
    if(type_input=="z"):
        subdirs=[]
        for root, dirs, files in os.walk(main_dir):
            for file in files:
                print(os.path.join(root, file))
                if (zipfile.is_zipfile(os.path.join(root, file))):
                 subdirs.append(os.path.join(root, file))
    elif (type_input=="d"):
        subdirs = [f.path for f in os.scandir(main_dir) if f.is_dir()]


    num_dir_per_process=len(subdirs)//num_process
    num_last_file=num_dir_per_process+(len(subdirs)%num_process)

    if os.path.exists("output"):
        shutil.rmtree('output')
    os.makedirs("output")

    for i in range(num_process):
        file = open("output/" + str(i + 1) + ".txt", "w")
        numWritten=0
        for j in range((i*num_dir_per_process),len(subdirs)):
            file.write(subdirs[j]+"\n")
            numWritten+=1
            if(numWritten==(num_dir_per_process) and i!=(num_process-1)):
                break
        file.close()
    for file in os.listdir("output/"):
        mode="zip"
        if type_input=="z":
            mode="zip"
        elif type_input=="d":
            mode="dir"
        command = " java -jar ../java-parser/dist/metricCalculator.jar {filename} {mode}".format(
            filename=full_file_path("output/"+file),mode=mode)
        command_params = command.split()
        run_command(
            command_params, full_file_path("metric.out"), full_file_path("metric.err"))
