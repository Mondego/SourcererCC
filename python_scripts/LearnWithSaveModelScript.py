import pandas as pd
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.tree import DecisionTreeClassifier

import pickle

print("imports complete")
path_train="./train/train_equal_cloneNonClone.txt"
path_test="./test/train_sample_100k.txt"
colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]
clones_train = pd.read_csv(path_train, names=colNames)
print("train set read complete")
clones_test = pd.read_csv(path_test, names=colNames)
print("test set read complete")

clones_train = clones_train.sample(frac=1).reset_index(drop=True) #shuffle data

array = clones_train.values
X_train = array[:,3:30]
Y_train = array[:,2]

array = clones_test.values
X_test = array[:,3:30]
Y_test = array[:,2]


#Learn Decision Tree
clf = DecisionTreeClassifier()
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
#Save model
filename = 'cart_model.sav'
pickle.dump(clf, open(filename, 'wb'))
print("model saved")

# load the model from disk
start_time = time.time()
loaded_model = pickle.load(open(filename, 'rb'))
# result = loaded_model.score(X_test, Y_test.astype(bool))
# print(result)
predictions = loaded_model.predict(X_test)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_test.astype(bool), predictions))
print(classification_report(Y_test.astype(bool), predictions))