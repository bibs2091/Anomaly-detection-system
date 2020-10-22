from flask import Flask, render_template
from flask_restful import Resource, Api
from py4j.java_gateway import JavaGateway

status = 'off'

def create_app(test_config=None):
	global status
	app = Flask(__name__, instance_relative_config=True)
	@app.route('/',)
	def index():
		data = {'status':status}
		return render_template("index.html",**data)

	@app.route('/start')
	def start():
		global status
		print("Starting IDS")
		status = 'on'
		gateway = JavaGateway()
		app = gateway.entry_point
		app.startTrafficFlow()
		data = {'status':status}
		return render_template("index.html",**data)

	@app.route('/stop')
	def stop():
		global status
		print("Stopping")
		status = 'off'
		gateway = JavaGateway()
		app = gateway.entry_point
		app.stopTrafficFlow()
		data = {'status':status}
		return render_template("index.html",**data)
	
	

	return app
print(status)
app = create_app()
app.run(host='0.0.0.0', port=7777)