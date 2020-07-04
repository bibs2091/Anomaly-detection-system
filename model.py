import pickle
import pandas as pd
import numpy as np
import logging

class model:
    def __init__(self):
        # load the pretrained model 
        try:
            self.model = pickle.load(open('decision_tree_model.pickle','rb'))
        except:
            # error if model can't be found in the path
            logging.error("Model can\'t be found in the main directory")
            logging.error("please fix the problem and restart the server")

        # load the features for the preprocessing step
        try:
            features_file = open("features.txt", "r")
            self.features = []
            for feature in features_file:
                self.features.append(feature)
            print("1")
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
        #remove the column with std equal to zero
        data = data.loc[:, data.std() > 0]
        #return the data as numpy array
        return data.to_numpy()

    def load_data(self,path = 'data_examples/example.csv'):
        #load and preprocess the csv file
        self.data = pd.read_csv(path)
        self.data = self.preprocess(self.data)

    def predict(self):
        self.prediction = model.predict(self.data)
        return self.prediction

model()