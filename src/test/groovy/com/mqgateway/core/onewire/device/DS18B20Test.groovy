package com.mqgateway.core.onewire.device

import spock.lang.Specification
import spock.lang.Subject

class DS18B20Test extends Specification {
	@Subject
	DS18B20 device = new DS18B20("test_address")

	def "should return value read from file"() {
		given:
		def masterDirPath = File.createTempDir().absolutePath
		new File("$masterDirPath/test_address").mkdir()
		def valueFilePath = "$masterDirPath/test_address/${device.deviceValueFileName$mqgateway()}"
		def valueFile = new File(valueFilePath)
		valueFile.createNewFile()
		valueFile.text = "6b 01 4b 46 7f ff 00 10 b6 : crc=b6 YES\n" +
			             "6b 01 4b 46 7f ff 00 10 b6 t=22687"


		when:
		def value = device.readValue(masterDirPath)

		then:
		value == "22687"
	}

	def "should return null when slave device file does not exist"() {
		given:
		def masterDirPath = File.createTempDir().absolutePath
		new File("$masterDirPath/test_address").mkdir()

		when:
		def value = device.readValue(masterDirPath)

		then:
		value == null
	}

	def "should return null when CRC check failed"() {
		given:
		def masterDirPath = File.createTempDir().absolutePath
		new File("$masterDirPath/test_address").mkdir()
		def valueFilePath = "$masterDirPath/test_address/${device.deviceValueFileName$mqgateway()}"
		def valueFile = new File(valueFilePath)
		valueFile.createNewFile()
		valueFile.text = "6b 01 4b 46 7f ff 00 10 b6 : crc=b6 NO\n" +
			"6b 01 4b 46 7f ff 00 10 b6 t=22687"


		when:
		def value = device.readValue(masterDirPath)

		then:
		value == null
	}
}
