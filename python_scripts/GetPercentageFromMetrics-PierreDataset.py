import pandas as pd
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import AdaBoostClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.ensemble import RandomForestClassifier
import pickle


path_input="/lv_scratch/scratch/mondego/local/farima/new_oreo/train_related/sampleTrainInput/th60_files/pierre/train_20per.txt"

path_output="/scratch/mondego/local/farima/new_oreo/train_related/sampleTrainInput/th60_files/pierre/train_20Per_byPercentageDiff.txt";
file_output = open(path_output, 'w')

with open(path_input) as input_file:
    for line in input_file:
        linesplitted=line.split("~~")
        row_to_write=linesplitted[0]+"~~"+linesplitted[1]+"~~"+linesplitted[2];
        for i in range(3,27):
            first_num=float(linesplitted[i])
            second_num=float(linesplitted[i+24])
            per_diff=round((abs((first_num-second_num)/(max(first_num,second_num)+11))*100),3)
            row_to_write=row_to_write+"~~"+str(per_diff)
        file_output.write(row_to_write+'\n')
file_output.close()
