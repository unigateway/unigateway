package com.mqgateway

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject
import spock.lang.Specification

/**
 * These tests can only be started with ENV SimulatedPlatform=com.pi4j.gpio.extension.mcp.MCP23017GpioProvider
 */
@MicronautTest
class MqGatewaySpec extends Specification {

	@Inject
	EmbeddedApplication application

	void 'test it works'() {
		expect:
		application.running
	}

}