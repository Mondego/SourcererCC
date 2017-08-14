import pandas as pd
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.linear_model import SGDClassifier
from sklearn.linear_model import PassiveAggressiveClassifier
from sklearn.linear_model import Perceptron
import numpy
import pickle

print("imports complete")
#path_train="./train/train_equal_cloneNonClone.txt"
#path_test="./test/train_sample_100k.txt"
path_train="D:\\PhD\\Clone\\\MlCC\\train_samples\\train_equal_cloneNonClone.txt"
path_train="D:\\PhD\\Clone\\\MlCC\\train_samples\\train_sample_100k.txt"
path_test="D:\\PhD\\Clone\\\MlCC\\train_samples\\train_sample_100k.txt"
colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]

clones_test = pd.read_csv(path_test, names=colNames)
array = clones_test.values
X_test = array[:,3:30]
Y_test = array[:,2]
print("test loaded")

chunkSize=1024
#clf=SGDClassifier()
#clf=PassiveAggressiveClassifier()
clf=Perceptron()
for chunk in pd.read_csv(path_train, names=colNames, chunksize=chunkSize):
    chunk = chunk.sample(frac=1).reset_index(drop=True)  # shuffle data
    array = chunk.values
    X_train = array[:, 3:30]
    Y_train = array[:, 2]
    start_time = time.time()
    model =clf.partial_fit(X_train,Y_train,classes=numpy.unique(Y_train.astype(bool)))
    end_time=time.time()
    print("one chunk complete")

filename = 'sgd_model.sav'
pickle.dump(clf, open(filename, 'wb'))
print("model saved")

# load the model from disk
start_time = time.time()
loaded_model = pickle.load(open(filename, 'rb'))
# result = loaded_model.score(X_test, Y_test.astype(bool))
# print(result)
for chunk in pd.read_csv(path_test, names=colNames, chunksize=chunkSize):
    print("chunk read complete")
    array = chunk.values
    X_test = array[:, 3:30]
    Y_test = array[:, 2]
    start_time = time.time()
    predictions = loaded_model.predict(X_test)
    end_time=time.time()
    print("time to predict: "+str((end_time-start_time)))
    print(confusion_matrix(Y_test.astype(bool), predictions))
    print(classification_report(Y_test.astype(bool), predictions))

#90-10 validation
clones_train = pd.read_csv(path_train, names=colNames)
array = clones_train.values
X_train = array[:,3:30]
Y_train = array[:,2]
lastIndex=0;
clf=Perceptron()
for i in range(0,X_train.shape[0]-(chunkSize),chunkSize):
    print(i)
    print(i+chunkSize)
    print(X_train.shape[0]-(chunkSize))
    x_train = X_train[i:i+(chunkSize),:]
    y_train = Y_train[i:i+(chunkSize),]
    start_time = time.time()
    model = clf.partial_fit(x_train,y_train, classes=numpy.unique(y_train.astype(bool)))
    end_time = time.time()
    print("one chunk complete")
    lastIndex=i

start_time = time.time()
predictions = clf.predict(X_train[lastIndex+(chunkSize):X_train.shape[0],])
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_train[lastIndex+(chunkSize):X_train.shape[0],].astype(bool), predictions))
