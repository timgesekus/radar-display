/**
 * 
 */

$(function() {

	var stage = new createjs.Stage("radarCanvas");
	var eventService = function() {
		var service = {};

		service.connect = function() {
			if (service.ws) {
				return;
			}
			var websocketUrl = jsRoutes.controllers.Events.events().webSocketURL();
			var ws = new WebSocket(websocketUrl);

			ws.onopen = function() {
				// service.callback("Succeeded to open a connection");
			};

			ws.onerror = function() {
				// service.callback("Failed to open a connection");
			}

			ws.onmessage = function(message) {
				service.callback(JSON.parse(message.data));
			};

			service.ws = ws;
		}

		service.send = function(message) {
			service.ws.send(JSON.stringify(message));
		}

		service.subscribe = function(callback) {
			service.callback = callback;
		}
		return service;
	};

	(function() {

		var Label = function(callsign, wtc) {
			this.initialize(callsign, wtc);
		};

		var p = Label.prototype = new createjs.Container();

		Label.prototype.Container_initialize = p.initialize;
		Label.prototype.initialize = function(callsign, wtc) {
			this.Container_initialize();
			var callSignText = new createjs.Text(callsign, "12px Courier", "#FFFFF");
			var wtcText = new createjs.Text(wtc, "12px Courier", "#FFFFF");
			wtcText.x = 90;
			this.addChild(callSignText);
			this.addChild(wtcText);
		};

		window.Label = Label;
	}());

	var myService = eventService();
	var labels = {};
	
	myService.subscribe(function(event) {
		var labelModel = event.content;
		if (event.name == "Add") {
			label = new Label(labelModel.values["CALLSIGN"].value, labelModel.values["WTC"].value);
			label.x = labelModel.x;
			label.y = labelModel.y;
			stage.addChild(label);
			console.log("Added");
			labels[labelModel.id]=label;
		}
		if (event.name == "Update") {
			var label = labels[labelModel.id];
			label.x = labelModel.x;
			label.y = labelModel.y;
			console.log("Updated");
		}
	
		stage.update();

	});
	myService.connect();
	stage.update();
});