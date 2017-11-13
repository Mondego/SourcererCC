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
modelfilename_type31 = '/scratch/mondego/local/model_type/randfor_type31_20es20ds10l5.sav'
modelfilename_type32 = '/scratch/mondego/local/model_type/randfor_type32_50es20ds10l5.sav'

# path_test="./test/test.txt"
# colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP",
# "COMP_", "NOCL_", "NOS_", "HLTH_", "HVOC_", "HEFF_", "HBUG_", "CREF_", "XMET_", "LMET_", "NLOC_", "NOC_", "NOA_", "MOD_", "HDIF_", "VDEC_", "EXCT_", "EXCR_", "CAST_", "TDN_", "HVOL_", "NAND_", "VREF_", "NOPR_", "MDN_", "NEXP_", "LOOP_"]

colNames=["block1","block2", "COMP", "NOS", "HVOC", "HEFF", "CREF", "XMET", "LMET",
          "NOA", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]#block1 (or 2) is directory,file,startline,endline

#load model
loaded_model_type31 = pickle.load(open(modelfilename_type31, 'rb'))
loaded_model_type32 = pickle.load(open(modelfilename_type32, 'rb'))
print("models loaded")
# data='com.liferay.portal.lar.PermissionImporter.importPermissions_6,com.liferay.portlet.wiki.action.ExportPageAction.getFile,0,33.33,0.0,22.22,21.6,13.59,9.89,24.0,9.52,8.7,100.0,17.24,0.0,11.11,0.0,31.58,0.0,0.0,0.0,0.0,42.86,24.07,22.22,27.42,20.89,33.33,36.36,100.0'
# data=data.replace(',','~~')

file_type1 = open('clonepairs_type1.txt', 'w')
file_type2 = open('clonepairs_type2.txt', 'w')

num_candidates_31=0
num_candidates_32=0
class predThread(threading.Thread):
    def __init__(self):
        super(predThread, self).__init__()

    def __init__(self,threadId,array,type):
        super(predThread, self).__init__()
        self.threadId=threadId
        self.array=array
        self.type=type

    def run(self):
        print('thread' + str(self.threadId)+' started')
        self.predict(self.type)
        print('thread ' + str(self.threadId) + ' ended')

    def predict(self,type):
        start_process_pred = time.time()
        file_clonepair = open('clonepairs' + str(self.threadId) + '_type_'+type+'.txt', 'w')
        #file_recall = open('recall' + str(self.threadId) + '_type_'+type+'.txt', 'w')
        #file_falsepos=open('falsepos' + str(self.threadId) + '_type_'+type+'.txt', 'w')
        #file_falseneg = open('falseneg' + str(self.threadId) + '_type_'+type+'.txt', 'w')
        clone_pairs = ''
        falsepos=''
        falseneg=''
        array_pred = np.array(self.array)
        # label = bool(int(array[2]))
        # X_test = array_pred[:, [i for i in range(0, 30+27) if i not in [0, 1, 2, 4,4+27,5+27,8+27,13,13+27,14,14+27,16,16+27,23+27]]]
        X_test = array_pred[:, [i for i in range(2, 20)]]
        X_test = X_test.astype(float)
        # Y_test = array_pred[:, 2].astype(bool)
        # methodpair = array[[i for i in range(0, 2)]]
        # X_test = np.reshape(X_test, (1, len(X_test)))
        print('prediction is gonna start')
        start_prediction = time.time()
        if type=='31':
            predictions = loaded_model_type31.predict(X_test)
        elif type=='32':
            predictions = loaded_model_type32.predict(X_test)
        end_prediction = time.time()
        print("prediction complete! time taken: " + str(end_prediction - start_prediction))
        #print("candidates processed: "+str(num_candidates))
        #file_recall.write(classification_report(Y_test, predictions))
        #file_recall.close()
        for i in range(predictions.shape[0]):
            if predictions[i]:
                clone_pairs += (array_pred[i][0] + ',' + array_pred[i][1]+'\n')
                #if not Y_test[i]:
                #    falsepos+=(array_pred[i][0] + ',' + array_pred[i][1])
                #    for j in range(0, 30+27):
                #        if j not in [0, 1, 2,4,4+27,5+27,8+27,13,13+27,14,14+27,16,16+27,23+27]:
                #            falsepos+=','+array_pred[i][j]
                #    falsepos=falsepos[:-1]+'\n'
            #if not predictions[i]:
                #if Y_test[i]:
                #    falseneg+=(array_pred[i][0] + ',' + array_pred[i][1])
                #    for j in range(0, 30+27):
                #        if j not in [0, 1, 2,4,4+27,5+27,8+27,13,13+27,14,14+27,16,16+27,23+27]:
                #            falseneg+=','+array_pred[i][j]
                #    falseneg=falseneg[:-1]+'\n'
        file_clonepair.write(clone_pairs)
        file_clonepair.close()
        #file_falsepos.write(falsepos)
        #file_falsepos.close()
        #file_falseneg.write(falseneg)
        #file_falseneg.close()
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
array_31=[]
array_32=[]


def process(data):
    global thread_counter,num_candidates_31,num_candidates_32
    global array_31
    global array_32
    global file_type1, file_type2
    #start_process = time.time()
    if "FINISHED_JOB" in data:
        print("last prediction started")
        thread_counter += 1
        # _thread.start_new_thread(predict(),array,thread_counter)
        thread_31 = predThread(thread_counter, array_31,'31')
        thread_32 = predThread(thread_counter, array_32,'32')
        thread_31.start()
        thread_32.start()
        print("Reading Finished. Wait for last thread to finish...")
        return 0
    data_splitted=data.split('#$#')
    clone_pairs=data_splitted[1].split('~~')
    #print(data_splitted[0])
    if data_splitted[0]=='1':
        #print(data_splitted[0])
        file_type1.write(clone_pairs[0]+','+clone_pairs[1]+'\n')
    elif data_splitted[0]=='2':
        #print(data_splitted[0])
        file_type2.write(clone_pairs[0] + ',' + clone_pairs[1]+'\n')
    elif data_splitted[0]=='3.1':
        array_31.append(clone_pairs)
        num_candidates_31+=1
        if len(array_31)==1*1000*1000:
            print("prediction started")
            thread_counter+=1
            #_thread.start_new_thread(predict(),array_31,thread_counter)
            thread=predThread(thread_counter,array_31,'31')
            thread.start()
            array_31=[]
    elif data_splitted[0]=='3.2':
        array_32.append(clone_pairs)
        num_candidates_32+=1
        if len(array_32)==1*1000*1000:
            print("prediction started")
            thread_counter+=1
            #_thread.start_new_thread(predict(),array_32,thread_counter)
            thread=predThread(thread_counter,array_32,'32')
            thread.start()
            array_32=[]
    #end_process = time.time()
    #print("process time: " + str(end_process - start_process))
    return 1


serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.bind(('localhost', 9901))
serversocket.listen(1) # maximum  1 connection
connection, address = serversocket.accept()
print("Connection accepted")
data=""
chunkcounter=0;
linecounter=0;
breakOuterLoop=False
while True:
    if breakOuterLoop:
        break
    chunkcounter+=1
    chunk = connection.recv(1024*1000*1000*2)
    print("chunk "+str(chunkcounter)+" read")
    chunk = chunk.decode('utf-8')
    if chunk and len(chunk) > 0:
        data = "{d}{c}".format(d=data,c=chunk)
        if "\n" in data:
            lines = data.split("\n")
            for index in range(0,len(lines)-1):
                linecounter+=1
                if process(lines[index])==0:
                    breakOuterLoop=True
                    break
            data = lines[index+1]
    else:
        break
end_time = time.time()
file_type1.close()
file_type2.close()
print("whole process took: "+str(end_time-start_time))
print("finished at: "+str(end_time))
print("total pairs analyzed: "+str(linecounter))
