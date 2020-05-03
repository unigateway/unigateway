package com.mqgateway.core.onewire.device

import static com.mqgateway.core.onewire.OneWireBusDeviceConnectionEvent.ConnectionEventType.CONNECTED
import static com.mqgateway.core.onewire.OneWireBusDeviceConnectionEvent.ConnectionEventType.DISCONNECTED

import com.mqgateway.core.onewire.OneWireBusDeviceStub
import spock.lang.Specification

class OneWireBusDeviceTest extends Specification {

	def "should notify about connection problems when trying to check value and device is unable to return it"() {
		given:
		def tmpDirPath = File.createTempDir().absolutePath
		boolean isDisconnected = false
		OneWireBusDeviceStub deviceStub = new OneWireBusDeviceStub("12345678")
		deviceStub.addDeviceConnectionListener { event -> if (event.type == DISCONNECTED) isDisconnected = true }
		deviceStub.checkValue(tmpDirPath)
		deviceStub.setReturnValue { return null }

		when:
		deviceStub.checkValue(tmpDirPath)

		then:
		isDisconnected
	}

	def "should notify about connection restored when connection has been restored"() {
		given:
		def tmpDirPath = File.createTempDir().absolutePath
		boolean hasBeenReconnected = false
		OneWireBusDeviceStub deviceStub = new OneWireBusDeviceStub("12345678")
		deviceStub.addDeviceConnectionListener { event -> if (event.type == CONNECTED) hasBeenReconnected = true }
		deviceStub.checkValue(tmpDirPath)
		deviceStub.setReturnValue { return null }
		deviceStub.checkValue(tmpDirPath)
		deviceStub.setReturnValue { return "good value" }

		when:
		deviceStub.checkValue(tmpDirPath)

		then:
		hasBeenReconnected
	}

	def "should notify listeners exactly once about new value when new value has been read"() {
		given:
		def tmpDirPath = File.createTempDir().absolutePath
		OneWireBusDeviceStub deviceStub = new OneWireBusDeviceStub("12345678")
		List<String> valuesRead = []
		deviceStub.addValueReceivedListener { event -> valuesRead.add(event.newValue) }
		deviceStub.setReturnValue { return "12345" }

		when:
		deviceStub.checkValue(tmpDirPath)
		deviceStub.checkValue(tmpDirPath)
		deviceStub.checkValue(tmpDirPath)

		then:
		valuesRead.size() == 1
		valuesRead[0] == "12345"
	}
}
