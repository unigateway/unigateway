package com.mqgateway.core.device

import com.mqgateway.core.onewire.OneWireBusDeviceStub
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class DS18B20DeviceTest extends Specification {

	private OneWireBusDeviceStub oneWireBusDeviceStub = new OneWireBusDeviceStub("123456")

	@Subject
	DS18B20Device device = new DS18B20Device("test_DS18B20Device", oneWireBusDeviceStub)

	def "should notify listeners on new value received"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
		oneWireBusDeviceStub.setReturnValue { return "4567" }

		when:
		oneWireBusDeviceStub.checkValue("masterDirPath")

		then:
		listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("test_DS18B20Device", "temperature", "4567")
	}
}
