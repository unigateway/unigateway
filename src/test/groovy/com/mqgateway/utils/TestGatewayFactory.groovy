package com.mqgateway.utils


import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.Point
import com.mqgateway.core.gatewayconfig.Room

class TestGatewayFactory {

	static Gateway gateway(List<Room> rooms) {
		new Gateway("1.0", "gtwName", "127.0.0.1", rooms)
	}

	static Room room(String name = "room name", List<Point> points) {
		new Room(name, points)
	}

	static Point point(String name = "point name", int portNumber = 1, List<DeviceConfig> devices) {
		new Point(name, portNumber, devices)
	}

}
