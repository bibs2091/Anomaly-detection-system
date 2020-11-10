import pandas as pd
import numpy as np
import logging
import sklearn
from joblib import load
import sys
import warnings
import os

if not sys.warnoptions:
    warnings.simplefilter("ignore")

class model:
    def __init__(self):
        #from number to labels
        self.number_to_label = {1 : "Bot",2 : 'DoS attack',3 : 'Brute Force', 5 : 'DDoS attacks',4 : 0}
        # load the pretrained model 
        try:
            self.model = load('./decision_tree_model.joblib')
            self.attack_model = load('./attack_model.joblib')
        except:
            # error if model can't be found in the path
            logging.error("Model can\'t be found in the main directory")
            logging.error("please fix the problem and restart the server")

        # load the features for the preprocessing step
        try:
            self.all_features = open("./all_features.txt", "r").readline().split(',')
            self.features = open("./features.txt", "r").read().splitlines()
        except:
            # error if features file can't be found in the path
            logging.error("features.txt can\'t be found in the main directory")
            logging.error("please fix the problem and restart the server")

    def preprocess(self,data):
        #select only the columns that works best with the pretrained model
        data = data[self.features]
        #remove infinite and null values
        data = data.replace([np.inf, -np.inf], np.nan)
        data = data.dropna()
        #change the type of the data to float
        data = data.astype("float")
        #return the data as numpy array
        return data.to_numpy()

    def load_data_csv(self,path = './data_examples/example.csv'):
        #load and preprocess the csv file
        self.data = pd.read_csv(path)
        #for evaluation tasks, we will save the label
        if ('Label' in self.data.columns):
            self.label = self.data['Label'].to_numpy()
        else:
            self.label = None
            logging.info('This data is labeled')

        self.data = self.preprocess(self.data)

    def load_data(self, rows) :
        #Load and preprocess strings in csv format 
        self.data =pd.DataFrame([x.strip(',').split(',') for x in rows.strip('bpoint').split('bpoint')],columns = self.all_features)
        self.data = self.preprocess(self.data)

    def predict(self):
        results = []
        #predict the class of the flow
        self.prediction = self.model.predict(self.data).astype('int32')
        #in case of one row prediction
        if (self.prediction.shape[0] == 1 ):
            if (self.prediction.item() == 1):
                results.append(self.number_to_label[self.attack_model.predict(self.data[0,:].reshape(1, -1)).item()])
            else:
                results.append(0)
                
        else:
            for i in range(self.prediction.shape[0]):
                if (self.prediction[i] == 1):
                    results.append(self.number_to_label[self.attack_model.predict(self.data[i,:].reshape(1, -1)).item()])
                else:
                    results.append(0)
        return results
    
    def accuracy(self):
        #calculate accuracy in case of label availaiblity
        if (self.label is None):
            logging.error("Score can't be calculated, No label provided")
            logging.error("be sure to name your label column with 'Lebel'")
            return None
        else:
            from sklearn.metrics import accuracy_score
            accuracy = accuracy_score(self.label, self.prediction)
            return accuracy
"""
m = model()
m.load_data(sys.argv[1])
prediction = m.predict()
"""


