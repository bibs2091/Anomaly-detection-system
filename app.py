from flask import Flask, render_template, request
from flask_socketio import SocketIO, emit
from py4j.java_gateway import JavaGateway
from pwn import process

status = 'off'

def create_app(test_config=None):
	global app_get 
	global model_server
	global req
	global data

	data = {
	"dos":0,
	"0":0
	}

	req = False
	model_server = process('./server.py')
	geteway = JavaGateway()
	app_get = geteway.entry_point
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
	@socketio.on('connect')
	def test_connect():
		pass

	@socketio.on('disconnect')
	def test_disconnect(): 
		print('Client disconnected')
	@socketio.on('request_predection')
	def request_handle():
		global model_server
		global req
		req = True
		while req:
			results = model_server.recvline().decode()
			for i in data.keys():
				if data[i] > 0:
					data[i] -= 1
			if results == "exit":
				quit()
			elif "DoS attack" in results:
				data["dos"] = (data["dos"] + 2)
			print(results)
			print(data)
			emit('predection', {'result': results})
			socketio.sleep(2)
	
	@socketio.on('stop_predection')
	def stopping():
		global req
		req = False
		print(req)


	return [socketio, app]
if __name__ == '__main__':
	socketio, app = create_app()
	socketio.run(app,host='127.0.0.1', port=7777)
