from flask import Flask, render_template
from flask_restful import Resource, Api


class Ping(Resource):
	def get(self):
		return {'ping': 'pong'}


def create_app(test_config=None):
	app = Flask(__name__, instance_relative_config=True)
	api = Api(app)
	api.add_resource(Ping, '/ping')
	@app.route('/', methods=['GET'])
	def main():
		return render_template("main.html")

	
	

	return app
