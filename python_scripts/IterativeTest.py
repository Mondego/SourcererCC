import pandas as pd
import numpy as np
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix

import pickle

print("imports complete")
chunkSize=1024*5
modelfilename = 'cart_model.sav'
path_test="C:\\clone_data\\test.txt"
path_test="D:\\PhD\\Clone\\MlCC\\train_samples\\train_sample_100k.txt"
# path_test="./test/test.txt"
colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]

#load model
loaded_model = pickle.load(open(modelfilename, 'rb'))
print("model loaded")

# clones_test = pd.read_csv(path_test, names=colNames)
# print("test set read complete")

# array = clones_test.values
# X_test = array[:, 3:30]
# Y_test = array[:, 2]
j=0
for chunk in pd.read_csv(path_test, names=colNames, chunksize=chunkSize):
    print("chunk read complete")
    chunk = chunk.sample(frac=1).reset_index(drop=True)  # shuffle data
    array = chunk.values
    X_test = array[:,[i for i in range(0,30) if i not in [2,4,14]]]
    Y_test = array[:, 2]
    start_time = time.time()
    predictions = loaded_model.predict(X_test[:,2:X_test.size])
    end_time=time.time()
    print("time to predict: "+str((end_time-start_time)))
    pred = np.reshape(predictions, (predictions.size, 1))
    result = np.concatenate((X_test[:, 0:2], pred), axis=1)
    # write falsepos to file
    falsepos = []
    for i in range(result.shape[0]):
        if int(result[i][2]) == 1 and Y_test[i] == 0:
            falsepos.append(result[i])
    np.savetxt('prediction'+str(j)+'.txt', falsepos, delimiter=',', fmt='%s')
    j=j+1
    # writing done
    print(confusion_matrix(Y_test.astype(bool), predictions))
    print(classification_report(Y_test.astype(bool), predictions))