import time
import threading


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
        time.sleep(5)
        print(self.array)


clones_pred=''
thread_counter=0
array=[]



def process():
    global array
    for i in range(100):
        array.append('hi')
    print(array)
    thread = predThread(i, array)
    thread.start()
    array = []

process()