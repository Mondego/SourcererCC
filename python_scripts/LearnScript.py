import pandas as pd
import numpy as np
import time as time
from random import randrange
#from pandas.tools.plotting import scatter_matrix
import matplotlib.pyplot as plt
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
from sklearn.ensemble import AdaBoostClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import make_scorer
import pickle

path="D:\\PhD\\Clone\\MlCC\\train_samples\\train_sample_100k.txt"
colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]
clones = pd.read_csv(path, names=colNames)



array = clones.values
X = array[:,[i for i in range(0,30) if i not in [2,4,14]]]
Y = array[:,2]
validation_size = 0.33
seed = 12
X_train, X_validation, Y_train, Y_validation = model_selection.train_test_split(X, Y, test_size=validation_size, random_state=seed)

#Random Forest
clf = RandomForestClassifier(n_estimators=5, max_depth=20, min_samples_split=10,min_samples_leaf=5)
start_time = time.time()
clf.fit(X_train[:,2:X_train.size], Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))

filename = 'cart_model.sav'
pickle.dump(clf, open(filename, 'wb'))
print("model saved")

start_time = time.time()
predictions = clf.predict(X_validation[:,2:X_validation.size])
#write results to file
pred=np.reshape(predictions,(predictions.size,1))
result=np.concatenate((X_validation[:,0:2],pred),axis=1)
falsepos=[]
for i in range(result.shape[0]):
    if int(result[i][2]) == 1 and Y_validation[i] == 0:
        falsepos.append(result[i])
np.savetxt('result.txt',falsepos,delimiter=',',fmt='%s,%s,%s')
#writing done
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_validation.astype(bool), predictions))
print(classification_report(Y_validation.astype(bool), predictions))

#Ada Boost
clf = AdaBoostClassifier(base_estimator=DecisionTreeClassifier(), n_estimators=5,  learning_rate=1)
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
start_time = time.time()
predictions = clf.predict(X_validation)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_validation.astype(bool), predictions))
print(classification_report(Y_validation.astype(bool), predictions))

#Decision Tree
clf = DecisionTreeClassifier()
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
start_time = time.time()
predictions = clf.predict(X_validation)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_validation.astype(bool), predictions))
print(classification_report(Y_validation.astype(bool), predictions))


#KNN
clf = KNeighborsClassifier(n_neighbors=10)
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
start_time = time.time()
predictions = clf.predict(X_validation)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_validation.astype(bool), predictions))
print(classification_report(Y_validation.astype(bool), predictions))

#LR
clf = LogisticRegression()
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
start_time = time.time()
predictions = clf.predict(X_validation)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_validation.astype(bool), predictions))
print(classification_report(Y_validation.astype(bool), predictions))

#LDA
clf = LinearDiscriminantAnalysis()
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
start_time = time.time()
predictions = clf.predict(X_validation)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_validation.astype(bool), predictions))
print(classification_report(Y_validation.astype(bool), predictions))

#NB
clf = GaussianNB()
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
start_time = time.time()
predictions = clf.predict(X_validation)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_validation.astype(bool), predictions))
print(classification_report(Y_validation.astype(bool), predictions))

#SVM
clf = SVC()
start_time = time.time()
clf.fit(X_train, Y_train.astype(bool))
end_time=time.time()
print("time to build model: "+str((end_time-start_time)))
start_time = time.time()
predictions = clf.predict(X_validation)
end_time=time.time()
print("time to predict: "+str((end_time-start_time)))
print(confusion_matrix(Y_validation.astype(bool), predictions))
print(classification_report(Y_validation.astype(bool), predictions))

#Cross Validation

models = []
models.append(('LR', LogisticRegression()))
models.append(('LDA', LinearDiscriminantAnalysis()))
models.append(('KNN 5', KNeighborsClassifier(n_neighbors=5)))
models.append(('KNN 10', KNeighborsClassifier(n_neighbors=10)))
models.append(('CART', DecisionTreeClassifier()))
models.append(('NB', GaussianNB()))
models.append(('Rand Forest', RandomForestClassifier(n_estimators=5)))
models.append(('Ada Boost', AdaBoostClassifier(base_estimator=DecisionTreeClassifier(),n_estimators=5,learning_rate=1)))
models.append(('SVM', SVC()))

def classification_report_with_accuracy_score(y_true, y_pred):
    print (classification_report(y_true, y_pred)) # print classification report
    return accuracy_score(y_true, y_pred)

results = []
names = []
for name, model in models:
    kfold = model_selection.KFold(n_splits=10, random_state=seed)
    print(name+"\n")
    start_time = time.time()
    cv_results = model_selection.cross_val_score(model, X_train, Y_train.astype(bool), cv=kfold, scoring=make_scorer(classification_report_with_accuracy_score))
    end_time = time.time()
    print("total time: " + str((end_time - start_time)))
    results.append(cv_results)
    names.append(name)
    msg = "%s: %f %f" % (name, cv_results.mean(), cv_results.std())
    print(msg)


def cross_validation_split(dataset, folds=10):
    dataset_split = list()
    dataset_copy = list(dataset)
    fold_size = int(len(dataset) / folds)
    for i in range(folds):
        fold = list()
        while len(fold) < fold_size:
            index = randrange(len(dataset_copy))
            fold.append(dataset_copy.pop(index))
        dataset_split.append(fold)
    return dataset_split

