import pickle
import pandas as pd
import numpy as np


model = pickle.load(open('decision_tree_model.pickle','rb'))
df = pd.read_csv("ktest0.csv")
to_predict = df.drop(['Label'], axis=1).to_numpy()
print(model.predict(to_predict))