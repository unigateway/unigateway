package com.mqgateway

import com.mqgateway.utils.MqttSpecification
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Timeout

@MicronautTest
class MqGatewaySpec extends MqttSpecification {

	@Inject
	EmbeddedApplication application

  @Timeout(30)
	void 'application is able to start'() {
		expect:
    application.running
	}
}
