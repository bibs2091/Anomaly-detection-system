# Anomaly-detection
Two parts:
    - Features extraction
    - Prediction

## 1-INTRODUCTION:


In this project we propose a solution for the cyber attacks on networks as a machine
learning based Intrusion detection system(IDS) and it's splitted to two parts:
### 1.1- Capturing the network flows and Features extraction:
>In this part we use [CICFlowMeter](https://github.com/ahlashkari/CICFlowMeter) to capture the flow and extract the features (83 feature),and tweaked its ui for a simpler usage.

### 1.2- Prediction:
>In order to achieve the highest accuracy we splitted the task into two stages to respectively detect the anomaly then classify it to a list of attacks we trained our model on, we used CSE-CIC-IDS2018 database to assure that we have the latest possible data on current cyber attacks.


## 2-Requirements:
- ### 2.1-For the Prediction Model:
    requirements.txt contains the needed python libraries.
    The needed python verson should be python3.*.
    ```
    $ pip install -r requirements.txt 
    ```
- ### 2.2-For CICFlowMeter:
    - Java jdk
    - check CICFlowMeter-master/README.md 

## 3-Installation and Configuration:
***
## 4-Running the project
### 4.1-Start the model server
```
$ python app.py
```
### 4.2-Start CICFlowMeter
#### 4.2.1-For Linux
```bash
cd CICFlowMeter-master/
sudo gradle
```
#### 4.2.2-For Windows
```dos
dir CICFlowMeter-master/
./gradlew execute
```
