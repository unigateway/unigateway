package com.mqgateway.core.device


import com.mqgateway.utils.DeviceStub
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification

class DeviceRegistryTest extends Specification {

	def "should initialize all devices"() {
		given:
		def devices = (1..4).collect { new DeviceStub("test_$it") }.toSet()
		DeviceRegistry deviceRegistry = new DeviceRegistry(devices)

		when:
		deviceRegistry.initailizeDevices()

		then:
		devices.every {
			it.initialized
		}
	}

	def "should add updateListener to each device"() {
		given:
		def devices = (1..4).collect { new DeviceStub("test_$it") }.toSet()
		DeviceRegistry deviceRegistry = new DeviceRegistry(devices)
		def updateListenerStub = new UpdateListenerStub()

		when:
		deviceRegistry.addUpdateListener(updateListenerStub)
		devices.each { it.notify("testProperty", "new value") }

		then:
		def expectedUpdates = (1..4).collect { new UpdateListenerStub.Update("test_$it", "testProperty", "new value") }
		updateListenerStub.receivedUpdates.toSet() == expectedUpdates.toSet()
	}
}


