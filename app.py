from flask import Flask, render_template, request
from flask_socketio import SocketIO, emit
from py4j.java_gateway import JavaGateway
# from pwn import process


status = 'off'

# Subtract and return  0 if negative 
def Subtract_Unless_0(data):
	for i in data.keys():
		data[i] = max(0,data[i]-1)
	return data

def data_processing(data,results):
	data = Subtract_Unless_0(data)
	if results == "exit":
		quit()
	else:
		for label in data.keys():
			if label in results:
				data[label] = min(data[label] + 2,10)
				break
	return data

def create_app(test_config=None):
	# global variabels
	global app_get 
	global model_server
	global req
	global data
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
	# model_server = process('./server.py')
	# init javagetway 
	geteway = JavaGateway()
	app_get = geteway.entry_point
	# init flask up
	app = Flask(__name__, instance_relative_config=True)
	socketio = SocketIO(app, logger=True)


	@app.route('/',)
	def index():
		return render_template("index.html")

	@app.route('/start',methods=['POST'])
	def start():
		global app_get
		print("Starting IDS")
		app_get.startTrafficFlow()
		return "0"

	@app.route('/stop',methods=['POST'])
	def stop():
		global app_get
		print("Stopping")
		app_get.stopTrafficFlow()
		return "0"
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
		# global model_server
		global req
		global data
		req = True

		# while req:
			# get the results
			# results = model_server.recvline().decode()
			# processing data and modfying it
			# depending on results
			# data = data_processing(data,results)
			# print(results)
			# print(data)
			# emit('predection', {'result': data})
			# socketio.sleep(2)
	

	@socketio.on('stop_predection')
	def stopping():
		global req
		req = False
		print(req)


	return [socketio, app]
if __name__ == '__main__':
	socketio, app = create_app()
	socketio.run(app,host='127.0.0.1', port=7777)
