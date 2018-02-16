import pandas as pd
import numpy as np
import time as time
import keras
from keras.models import load_model
import pickle

print("imports complete")
chunkSize=1024*5000
modelfilename = '/scratch/mondego/local/farima/tensorFlow/experiments/models/trained_model.h5'
path_test="/lv_scratch/scratch/mondego/local/farima/new_oreo/train_related/sampleTrainInput/th60_files/pierre/train_20Per_byPercentageDiff.txt"
path_output="/scratch/mondego/local/farima/tensorFlow/experiments/results/first_model.txt"
# path_test="./test/test.txt"
file_output = open(path_output, 'w')
colNames=["block1","block2", "isClone", "COMP1", "NOS1", "HVOC1", "HEFF1", "CREF1", "XMET1", "LMET1","NOA1", "HDIF1", "VDEC1", "EXCT1", "EXCR1", "CAST1",
          "NAND1", "VREF1", "NOPR1", "MDN1", "NEXP1", "LOOP1","NBLTRL1","NCLTRL1","NNLTRL1","NNULLTRL1","NSLTRL1","COMP2", "NOS2", "HVOC2", "HEFF2", "CREF2",
          "XMET2", "LMET2","NOA2", "HDIF2", "VDEC2","EXCT2", "EXCR2", "CAST2", "NAND2", "VREF2", "NOPR2", "MDN2", "NEXP2", "LOOP2","NBLTRL2","NCLTRL2","NNLTRL2",
          "NNULLTRL2","NSLTRL2"]

#load model
model = load_model(path_output)
print("model loaded")

clones_test = pd.read_csv(path_test, names=colNames, delimiter='~~', engine='python')
print("test set read complete")

array = clones_test.values
X_test = array[:,[i for i in range(3,51)]]
Y_test = array[:, 2]
start_time = time.time()
pred = model.predict( X_test, batch_size=1000, verbose=0 )
y_pred_classes = np.zeros_like(pred)
index = pred>0.5
y_pred_classes[index] = 1
#predictions = loaded_model.predict(X_test)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
# file_output.write(confusion_matrix(Y_test.astype(bool), y_pred_classes))

file_output.close()
