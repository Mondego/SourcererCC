import pandas as pd
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier
import pickle

print("imports complete")
path_train="/lv_scratch/scratch/mondego/local/farima/new_oreo/train_related/sampleTrainInput/th60_files/pierre/train_80Per_byPercentageDiff.txt"
#path_test="./test/train_sample_100k.txt"
colNames=["block1","block2", "isClone", "COMP", "NOS", "HVOC", "HEFF", "CREF", "XMET", "LMET","NOA", "HDIF", "VDEC","EXCT", "EXCR", "CAST", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP","NBLTRL","NCLTRL","NNLTRL","NNULLTRL","NSLTRL"]
path_output="/scratch/mondego/local/farima/new_oreo/train_related/train_models/";
clones_train = pd.read_csv(path_train, names=colNames, delimiter='~~', engine='python')
print("train set read complete")
#clones_test = pd.read_csv(path_test, names=colNames)
#print("test set read complete")

clones_train = clones_train.sample(frac=1).reset_index(drop=True) #shuffle data

array = clones_train.values
X_train = array[:,[i for i in range(3,27)]]
Y_train = array[:,2]

#array = clones_test.values
#X_test = array[:,3:30]
#Y_test = array[:,2]


#Learn Decision Tree
clf = RandomForestClassifier(n_estimators=25, max_depth=15)
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
#Save model
filename = 'randFoer_Pierre_Dataset.sav'
pickle.dump(clf, open(path_output+filename, 'wb'))
print("model saved")

# load the model from disk
#start_time = time.time()
#loaded_model = pickle.load(open(filename, 'rb'))
# result = loaded_model.score(X_test, Y_test.astype(bool))
# print(result)
#predictions = loaded_model.predict(X_test)
#end_time=time.time()
#print("time to predict: "+str((end_time-start_time)))
#print(confusion_matrix(Y_test.astype(bool), predictions))
#print(classification_report(Y_test.astype(bool), predictions))
