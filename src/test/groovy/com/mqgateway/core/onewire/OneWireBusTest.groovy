package com.mqgateway.core.onewire

import java.util.concurrent.TimeUnit
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

class OneWireBusTest extends Specification {

	def "should check value for all registered devices on given interval"() {
		given:
		File tempDir = File.createTempDir()
		BlockingVariable<String> blockingVariable = new BlockingVariable<>(10, TimeUnit.SECONDS)
		long testStartTimestamp = System.currentTimeMillis()
		def oneWireBus = new OneWireBus(tempDir.absolutePath, 5)
		def fakeDevice = new OneWireBusDeviceStub("123456")
		fakeDevice.addDeviceConnectionListener {event -> blockingVariable.set("this is new value") }
		oneWireBus.registerDevice(fakeDevice)

		when:
		oneWireBus.start()

		then:
		blockingVariable.get() == "this is new value"
		testStartTimestamp + 100 > System.currentTimeMillis()
	}
}

