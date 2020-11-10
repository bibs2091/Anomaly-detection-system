from flask import Flask, render_template, request, jsonify
from flask_socketio import SocketIO, emit
from py4j.java_gateway import JavaGateway
from pwn import process
import json

status = 'off'
#if data reset
reset = 0
# Subtract and return  0 if negative 
def Subtract_Unless_0(data):
    for i in data.keys():
        data[i] = max(0,data[i]-1)
    return data

def data_processing(data,results):
    #data = Subtract_Unless_0(data)
    if results == "exit":
        quit()
    else:
        for label in data.keys():
            if label in results:
                data[label] = data[label] + 1
                break
    return data

def create_app(test_config=None):
    # global variabels
    global app_get 
    global model_server
    global req
    global data
    global config
    # init attacks percentages 
    data = {
    "Bot":0,
    "DoS attack":0,
    "Brute Force":0,
    "DDoS attacks":0,
    "0":0
    }

    req = False
    # start model_server
    #model_server = process('./server.py')
    # init javagetway 
    geteway = JavaGateway()
    app_get = geteway.entry_point
    # init flask up
    app = Flask(__name__, instance_relative_config=True)
    socketio = SocketIO(app, logger=True)
    f = open('config.json','r')
    config = json.load(f)

    @app.route('/',)
    def index():
        global status
        global app_get
        global config
        if config['auto-start']==1:
            status = 'on'
            print("Starting IDS")
            app_get.startTrafficFlow()
        data = {"status":status,
                "auto_start":config["auto-start"],
                "level_threat":config["level-threat"],
                "reset_level":config["reset-level"]}
        return render_template("index.html",**data)

    @app.route('/start',methods=['POST'])
    def start():
        global app_get
        global status
        status = 'on'
        print("Starting IDS")
        app_get.startTrafficFlow()
        return "0"

    @app.route('/stop',methods=['POST'])
    def stop():
        global app_get
        global status
        status = 'off'
        print("Stopping")
        app_get.stopTrafficFlow()
        return "0"
    
    @app.route('/info/<attack>',)
    def info(attack):
        return render_template(f"{attack}.html")
    
    @app.route('/reset_traffic',methods=['POST'])
    def reset_traffic():
        global data
        global reset
        reset += 1
    # init attacks percentages 
        print("Data reset!")
        data = {
            "Bot":0,
            "DoS attack":0,
            "Brute Force":0,
            "DDoS attacks":0,
            "0":0
        }
        return "0"
    
    @app.route('/update_settings',methods=['GET','POST'])
    def update_settings():
        data = request.get_json()
        f = open('config.json','w')
        print(data)
        json.dump(data,f)
        f.close()
        return jsonify(status="success")
    
    @app.route('/post-predict',methods=['POST'])
    def postpredict():
        global data
        global reset
        global config
        received = request.get_json() 
        delta = reset*int(config['reset-level'])
        print("Data received!")
        for key in received:
            if int(received[key]) > delta:
                print("CONTINUE")
                print("Received->",received)
                print("Data->",data)
                continue
            data[key] = received[key] - delta
        #data.clear()
        #data = received
        print(data)
        return "1"
    
    # socketio events
    @socketio.on('connect')
    def test_connect():
        print('client connected')

    @socketio.on('disconnect')
    def test_disconnect(): 
        print('Client disconnected')

    # start sending predections from model_server to client
    @socketio.on('request_predection')
    def request_handle():
        global model_server
        global req
        global data
        req = True
        while req:
            try:
                #get the results
                emit('predection', {'result': data})
                socketio.sleep(3)
            except:
                pass

    @socketio.on('stop_predection')
    def stopping():
        global req
        req = False
        print(req)
    return [socketio, app]

if __name__ == '__main__':
    socketio, app = create_app()
    socketio.run(app,host='127.0.0.1', port=7777)
