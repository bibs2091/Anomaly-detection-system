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
        self.number_to_label = {1 : "Bot",2 : 'DoS attack',3 : 'Brute Force', 5 : 'DDoS attacks',4 : 'Infilteration',}
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
            features_file = open("./features.txt", "r")
            self.features = []
            for feature in features_file:
                self.features.append(feature.strip())
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

    def load_data(self, rows = "192.168.1.3-140.82.118.4-65394-443-6,192.168.1.3,65394,140.82.118.4,443,6,678,4,1,24.0,24.0,24.0,0.0,6.0,12.0,24.0,24.0,24.0,0.0,70796.46017699115,7374.6312684365785,169.5,151.22720213859233,385.0,50.0,678.0,226.0,185.47506570965274,435.0,81.0,0,0,0,0,0,0,0,0,0,80,20,5899.705014749263,1474.9262536873157,0.0,24.0,8.0,12.393546707863734,153.6,1,0,0,2,5,0,0,0,0.0,9.6,6.0,24.0,0,0,0,0,0,0,0,4,0,4,68,70,1,20,0,0,0,0,1.5940385198503995E15,351.4320702497141,1.594038519850648E15,1.594038519850151E15,NeedManualLabel") :
        #Load and preprocess strings in csv format 
        columns = open("./all_features.txt", "r").readline().split(',')
        self.data =pd.DataFrame([x.split(',') for x in rows.split('\n')],columns = columns)
        self.data = self.preprocess(self.data)

    def predict(self):
        #predict the class of the flow
        self.prediction = self.model.predict(self.data).astype('int32')
        #in case of one row prediction
        if (self.prediction.shape[0] == 1 ):
            if (self.prediction.item() == 1):
                print(self.number_to_label[self.attack_model.predict(self.data[0,:].reshape(1, -1)).item()])
            else:
                print(0)
                
        else:
            for i in range(self.prediction.shape[0]):
                if (self.prediction[i] == 1):
                    print(self.number_to_label[self.attack_model.predict(self.data[i,:].reshape(1, -1)).item()])
                else:
                    print(0)

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


