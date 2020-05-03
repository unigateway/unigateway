package com.mqgateway.core.device


import com.mqgateway.core.onewire.OneWireBusDeviceStub
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class OneWireDeviceTest extends Specification {

	private OneWireBusDeviceStub oneWireBusDeviceStub = new OneWireBusDeviceStub("123456")

	@Subject
	OneWireDevice device = new DS18B20Device("deviceId1", oneWireBusDeviceStub)

	def "should notify listeners when device has been disconnected"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
		oneWireBusDeviceStub.setReturnValue { "12345" }
		oneWireBusDeviceStub.checkValue("masterDirPath")
		oneWireBusDeviceStub.setReturnValue { null }


		when:
		oneWireBusDeviceStub.checkValue("masterDirPath")

		then:
		listenerStub.receivedUpdates.any { it == new UpdateListenerStub.Update("deviceId1", "state", "DISCONNECTED") }
	}

	def "should notify listeners when device has been reconnected"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
		oneWireBusDeviceStub.setReturnValue { null }
		oneWireBusDeviceStub.checkValue("masterDirPath")
		oneWireBusDeviceStub.setReturnValue { "11223" }


		when:
		oneWireBusDeviceStub.checkValue("masterDirPath")

		then:
		listenerStub.receivedUpdates.any { it == new UpdateListenerStub.Update("deviceId1", "state", "CONNECTED") }
	}
}
