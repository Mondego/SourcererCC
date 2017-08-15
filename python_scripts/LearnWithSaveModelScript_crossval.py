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

print("imports complete")
#path_train="./train/train_equal_cloneNonClone.txt"
path_train="D:\\PhD\\Clone\\\MlCC\\train_samples\\train_sample_10k.txt"
# path_test="./test/test.txt"
path_test="D:\\PhD\\Clone\\\MlCC\\train_samples\\train_sample_50k.txt"
colNames=["block1", "block2", "isClone", "COMP", "NOCL", "NOS", "HLTH", "HVOC", "HEFF", "HBUG", "CREF", "XMET", "LMET", "NLOC", "NOC", "NOA", "MOD", "HDIF", "VDEC", "EXCT", "EXCR", "CAST", "TDN", "HVOL", "NAND", "VREF", "NOPR", "MDN", "NEXP", "LOOP"]
clones_train = pd.read_csv(path_train, names=colNames)
print("train set read complete")
# clones_test = pd.read_csv(path_test, names=colNames)
# print("test set read complete")

array = clones_train.values
X_train = array[:,3:30]
Y_train = array[:,2]

# array = clones_test.values
# X_test = array[:,3:30]
# Y_test = array[:,2]


#Cross Validation

models = []
# models.append(('LR', LogisticRegression()))
# models.append(('LDA', LinearDiscriminantAnalysis()))
# models.append(('KNN 5', KNeighborsClassifier(n_neighbors=5)))
# models.append(('KNN 10', KNeighborsClassifier(n_neighbors=10)))
models.append(('CART', DecisionTreeClassifier()))
# models.append(('NB', GaussianNB()))
#models.append(('SVM', SVC()))

def classification_report_with_accuracy_score(y_true, y_pred):
    print (classification_report(y_true, y_pred)) # print classification report
    return accuracy_score(y_true, y_pred)

results = []
names = []
seed = 12
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
    print(name+" completed")

print("cross validation complete")
#Learn Decision Tree
# clf = DecisionTreeClassifier()
# start_time = time.time()
# clf.fit(X_train, Y_train.astype(bool))
# end_time=time.time()
# print("time to build model: "+str((end_time-start_time)))
# #Save model
# filename = 'cart_model2.sav'
# pickle.dump(clf, open(filename, 'wb'))
# print("model saved")
#
# # load the model from disk
# start_time = time.time()
# loaded_model = pickle.load(open(filename, 'rb'))
# # result = loaded_model.score(X_test, Y_test.astype(bool))
# # print(result)
# predictions = loaded_model.predict(X_test)
# end_time=time.time()
# print("time to predict: "+str((end_time-start_time)))
# print(confusion_matrix(Y_test.astype(bool), predictions))
# print(classification_report(Y_test.astype(bool), predictions))
