import pandas as pd
import time as time
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn import model_selection
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from sklearn.metrics import make_scorer
import pickle
from sklearn.cross_validation import KFold

path_train="D:\\PhD\\Clone\\\MlCC\\train_samples\\train_sample_100k.txt"
# path_test="./test/test.txt"
path_test="D:\\PhD\\Clone\\\MlCC\\train_samples\\train_sample_50k.txt"
colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]
clones_train = pd.read_csv(path_train, names=colNames)
print("train set read complete")
# clones_test = pd.read_csv(path_test, names=colNames)
# print("test set read complete")

array = clones_train.values
X = array[:,3:30]
Y = array[:,2]
kf = KFold(len(Y), 10)
print("index done")
for train, test in kf:
    X_train, X_test, Y_train, Y_test = X[train], X[test], Y[train], Y[test]
    clf = DecisionTreeClassifier(max_depth=20)
    start_time = time.time()
    clf.fit(X_train, Y_train.astype(bool))
    end_time = time.time()
    print("time to build model: " + str((end_time - start_time)))
    start_time = time.time()
    predictions = clf.predict(X_test)
    end_time = time.time()
    print("time to predict: " + str((end_time - start_time)))
    print(confusion_matrix(Y_test.astype(bool), predictions))
    print(classification_report(Y_test.astype(bool), predictions))
    X_train=[]
    X_test={}
    Y_train=[]
    Y_test=[]