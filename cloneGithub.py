import csv
import sys
import os
import subprocess
import shutil
from shlex import split


def doBashCommand(bashCommand):
    process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
    output, _ = process.communicate()
    return output


def removeFile(fileName, fileDir=os.path.realpath('.') + '/'):
    if os.path.exists(fileDir + fileName):
        os.remove(fileDir + fileName)


if __name__ == '__main__':
    csv.field_size_limit(sys.maxsize)
    goPath = doBashCommand("go env GOPATH")[:-1] + "/bin"
    projectsNamesToSivaNames = {}
    i = 0

    with open('sivaFilesNames.txt', 'w') as fout, open('index2.csv', 'r') as fin, open('nameToLicense.txt', 'w') as fout2:
        for row in csv.reader(fin, delimiter=','):
            # ADD FILTERS FOR PROJECTS HERE
            if (('Java,' in row[3])
                    and (row[13] == ''
                         or 'MIT' in row[13]
                         or 'GPL' in row[13]
                         or 'Apache' in row[13]
                         or 'BSD' in row[13])):
                i = i + 1
                fout2.write(row[0][19:].replace('/', '_') + ";" + row[13] + '\n')
                projectsNamesToSivaNames[row[0][19:].replace('/', '_')] = row[1]
                fout.write(row[1].replace(',', '\n') + '\n')
    p1 = subprocess.Popen(split("cat sivaFilesNames.txt"), stdout=subprocess.PIPE)
    p2 = subprocess.Popen(split(goPath + "/pga get -i -v"), stdin=p1.stdout)
    p2.communicate()

    startDir = os.path.realpath('.')
    projectsDir = startDir + '/projects/'
    sivaDir = startDir + '/siva/latest/'
    if not os.path.exists(projectsDir):
        os.makedirs(projectsDir)
    removeFile('sivaFilesNames.txt')

    sivaDirs = os.listdir(sivaDir)
    for folder in sivaDirs:
        if os.path.isdir(sivaDir + folder):
            for file in os.listdir(sivaDir + folder):
                shutil.copy(sivaDir + folder + '/' + file, sivaDir)

    for project in projectsNamesToSivaNames.keys():
        projectName = projectsDir + project
        os.makedirs(projectName + "/.git")
        os.chdir(projectName + "/.git")

        for sivaFile in projectsNamesToSivaNames[project].split(','):
            removeFile('HEAD', projectName + "/.git/")
            removeFile('config', projectName + "/.git/")
            doBashCommand(goPath + "/siva unpack " + sivaDir + sivaFile)
            removeFile(sivaFile, sivaDir)

        os.chdir(projectName)
        for line in doBashCommand("git branch").split('\n'):
            if 'HEAD' in line:
                doBashCommand("git checkout " + line)
                break
        os.chdir(projectsDir)
        doBashCommand("zip -r " + projectName + ".zip " + projectName)
        doBashCommand("rm -r " + projectName)
