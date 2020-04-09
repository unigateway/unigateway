package com.mqgateway


import spock.lang.Specification

/**
 * These tests can only be started with ENV SimulatedPlatform=com.pi4j.gpio.extension.mcp.MCP23017GpioProvider
 */
class ApplicationStartsTest extends Specification {

	def "should start application with simulated GPIOs"() {
		expect:
		MainKt.main("src/test/resources/example.homiegateway.yml")
	}
}
