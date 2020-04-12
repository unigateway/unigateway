package com.mqgateway


import spock.lang.Specification
import spock.lang.Timeout

/**
 * These tests can only be started with ENV SimulatedPlatform=com.pi4j.gpio.extension.mcp.MCP23017GpioProvider
 */
@Timeout(60)
class ApplicationStartsTest extends Specification {

	def "should start application with simulated GPIOs"() {
		expect:
		MainKt.main("src/test/resources/example.gateway.yaml")
	}
}
