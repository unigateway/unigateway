package com.mqgateway

import io.micronaut.context.annotation.Property
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Timeout

@MicronautTest
@Property(name = "gateway.mqtt.enabled", value = "false")
class MqGatewaySpec extends Specification {

	@Inject
	EmbeddedApplication application

  @Timeout(30)
	void 'application is able to start'() {
		expect:
    application.running
	}
}
