package com.mqgateway.homie.gateway

import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.utils.DeviceStub
import spock.lang.Specification

class GatewayHomieReceiverTest extends Specification {

	def "should change state in device from registry by given deviceId"() {
		given:
		Set<DeviceStub> devices = (1..10).collect { new DeviceStub("nodeId_$it") }.toSet()
		GatewayHomieReceiver receiver = new GatewayHomieReceiver(new DeviceRegistry(devices))
		def device4 = devices.find { it.id == "nodeId_4" }
		def device7 = devices.find { it.id == "nodeId_7" }

		when:
		receiver.propertySet("homie/deviceId/nodeId_4/property1/set", "new value")
		receiver.propertySet("homie/deviceId/nodeId_7/property3/set", "another value")

		then:
		device4.getValueForProperty("property1") == "new value"
		device7.getValueForProperty("property3") == "another value"
	}

	def "should throw exception when received property set message for unknown device"() {
		given:
		Set<DeviceStub> devices = (1..10).collect { new DeviceStub("nodeId_$it") }.toSet()
		GatewayHomieReceiver receiver = new GatewayHomieReceiver(new DeviceRegistry(devices))

		when:
		receiver.propertySet("homie/deviceId/someDevice/property1/set", "new value")

		then:
		thrown(DeviceNotFoundException)
	}
}
