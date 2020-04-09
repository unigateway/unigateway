package com.mqgateway.utils


import com.mqgateway.core.gatewayconfig.ComponentsConfiguration
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.Mcp23017Configuration
import com.mqgateway.core.gatewayconfig.Point
import com.mqgateway.core.gatewayconfig.Room
import com.mqgateway.core.gatewayconfig.SystemConfiguration
import com.mqgateway.core.gatewayconfig.SystemPlatform

class TestGatewayFactory {

	static Gateway gateway(List<Room> rooms) {
		def netInterface = NetworkInterface.getByIndex(1)
		def systemConfiguration = new SystemConfiguration(netInterface.name, SystemPlatform.SIMULATED,
														  new ComponentsConfiguration(new Mcp23017Configuration(["20", "21", "22", "23"])))
		new Gateway("1.0", "gtwName", "127.0.0.1", systemConfiguration, rooms)
	}

	static Room room(String name = "room name", List<Point> points) {
		new Room(name, points)
	}

	static Point point(String name = "point name", int portNumber = 1, List<DeviceConfig> devices) {
		new Point(name, portNumber, devices)
	}

}
