import pandas as pd
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.ensemble import RandomForestClassifier
import pickle

print("imports complete")
chunkSize=1024*5000
modelfilename = '/scratch/mondego/local/farima/new_oreo/train_related/train_models/randFoer_Pierre_Dataset.sav'
path_test="/lv_scratch/scratch/mondego/local/farima/new_oreo/train_related/sampleTrainInput/th60_files/pierre/train_20Per_byPercentageDiff.txt"
path_output="/scratch/mondego/local/farima/new_oreo/train_related/train_models/randFor_Pierre_Report.txt"
# path_test="./test/test.txt"
file_output = open(path_output, 'w')
colNames=["block1","block2", "isClone", "COMP", "NOS", "HVOC", "HEFF", "CREF", "XMET", "LMET","NOA", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP","NBLTRL","NCLTRL","NNLTRL","NNULLTRL","NSLTRL"]

#load model
loaded_model = pickle.load(open(modelfilename, 'rb'))
print("model loaded")

clones_test = pd.read_csv(path_test, names=colNames, delimiter='~~', engine='python')
print("test set read complete")

array = clones_test.values
X_test = array[:,[i for i in range(3,27)]]
Y_test = array[:, 2]
start_time = time.time()
predictions = loaded_model.predict(X_test)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
file_output.write(confusion_matrix(Y_test.astype(bool), predictions))
file_output.write(classification_report(Y_test.astype(bool), predictions))
file_output.close()
