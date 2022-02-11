package com.mqgateway.utils


import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.Point
import com.mqgateway.core.gatewayconfig.Room

class TestGatewayFactory {

	static GatewayConfiguration gateway(List<Room> rooms) {
		new GatewayConfiguration("1.0", "gtwName", "127.0.0.1", rooms)
	}

	static Room room(String name = "room name", List<Point> points) {
		new Room(name, points)
	}

	static Point point(String name = "point name", int portNumber = 1, List<DeviceConfiguration> devices) {
		new Point(name, portNumber, devices)
	}

}
