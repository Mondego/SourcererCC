import pandas as pd
import numpy as np
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
import pickle
import socket
import threading

start_time = time.time()
print("imports complete")
modelfilename = 'randforest_5est_d20_s10_l5.sav'
# path_test="./test/test.txt"
colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]

#load model
loaded_model = pickle.load(open(modelfilename, 'rb'))
print("model loaded")
# data='com.liferay.portal.lar.PermissionImporter.importPermissions_6,com.liferay.portlet.wiki.action.ExportPageAction.getFile,0,33.33,0.0,22.22,21.6,13.59,9.89,24.0,9.52,8.7,100.0,17.24,0.0,11.11,0.0,31.58,0.0,0.0,0.0,0.0,42.86,24.07,22.22,27.42,20.89,33.33,36.36,100.0'
# data=data.replace(',','~~')
num_candidates=0
class predThread(threading.Thread):
    def __init__(self):
        super(predThread, self).__init__()

    def __init__(self,threadId,array):
        super(predThread, self).__init__()
        self.threadId=threadId
        self.array=array

    def run(self):
        print('thread' + str(self.threadId)+' started')
        self.predict()
        print('thread ' + str(self.threadId) + ' ended')

    def predict(self):
        start_process_pred = time.time()
        file_clonepair = open('clonepairs' + str(self.threadId) + '.txt', 'w')
        file_recall = open('recall' + str(self.threadId) + '.txt', 'w')
        file_falsepos=open('falsepos' + str(self.threadId) + '.txt', 'w')
        clone_pairs = ''
        falsepos=''
        array_pred = np.array(self.array)
        # label = bool(int(array[2]))
        X_test = array_pred[:, [i for i in range(0, 30) if i not in [0, 1, 2, 4, 14]]]
        X_test = X_test.astype(float)
        Y_test = array_pred[:, 2].astype(bool)
        # methodpair = array[[i for i in range(0, 2)]]
        # X_test = np.reshape(X_test, (1, len(X_test)))
        print('prediction is gonna start')
        start_prediction = time.time()
        predictions = loaded_model.predict(X_test)
        end_prediction = time.time()
        print("prediction complete! time taken: " + str(end_prediction - start_prediction))
        print("candidates processed: "+str(num_candidates))
        file_recall.write(classification_report(Y_test, predictions))
        file_recall.close()
        for i in range(predictions.shape[0]):
            if predictions[i]:
                clone_pairs += (array_pred[i][0] + ',' + array_pred[i][1]+'\n')
                if not Y_test[i]: falsepos+=(array_pred[i][0] + ',' + array_pred[i][1]+'\n')
        file_clonepair.write(clone_pairs)
        file_clonepair.close()
        file_falsepos.write(falsepos)
        file_falsepos.close()
        # if label:
        #     num_clones = num_clones + 1
        #     if label == prediction:
        #         tp = tp + 1
        #     elif label != prediction:
        #         fn = fn + 1
        end_process_pred = time.time()
        print("pred and writing to file: " + str(end_process_pred - start_process_pred))


clones_pred=''
thread_counter=0
array=[]



def process(data):
    global thread_counter,num_candidates
    global array
    #start_process = time.time()
    if "FINISHED_JOB" in data:
        print("last prediction started")
        thread_counter += 1
        # _thread.start_new_thread(predict(),array,thread_counter)
        thread = predThread(thread_counter, array)
        thread.start()
        print
        np.array(list(data))
        print("Reading Finished. Wait for last thread to finish...")
        return 0
    array.append(data.split('~~'))
    num_candidates+=1
    if len(array)==10*1000*1000:
        print("prediction started")
        thread_counter+=1
        #_thread.start_new_thread(predict(),array,thread_counter)
        thread=predThread(thread_counter,array)
        thread.start()
        array=[]
    #end_process = time.time()
    #print("process time: " + str(end_process - start_process))
    return 1


serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.bind(('localhost', 9900))
serversocket.listen(1) # maximum  1 connection
connection, address = serversocket.accept()
print("Connection accepted")
data=""
while True:
    chunk = connection.recv(1024*1000*1000*2)
    print("one chunk read")
    chunk = chunk.decode('utf-8')
    if chunk and len(chunk) > 0:
        data = "{d}{c}".format(d=data,c=chunk)
        if "\n" in data:
            lines = data.split("\n")
            for index in range(0,len(lines)-1):
                if process(lines[index])==0:
                    break
            data = lines[index+1]
    else:
        break

end_time = time.time()
print("whole process took: "+str(end_time-start_time))
print("finished at: "+str(end_time))