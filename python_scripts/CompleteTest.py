import pandas as pd
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix

import pickle

print("imports complete")
chunkSize=1024*5000
modelfilename = 'cart_model2.sav'
path_test="C:\\clone_data\\test.txt"
# path_test="./test/test.txt"
colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]

#load model
loaded_model = pickle.load(open(modelfilename, 'rb'))
print("model loaded")

clones_test = pd.read_csv(path_test, names=colNames)
print("test set read complete")

array = clones_test.values
X_test = array[:, 3:30]
Y_test = array[:, 2]
start_time = time.time()
predictions = loaded_model.predict(X_test)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_test.astype(bool), predictions))
print(classification_report(Y_test.astype(bool), predictions))