package com.mqgateway

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject
import spock.lang.Specification

@MicronautTest
class MqGatewaySpec extends Specification {

	@Inject
	EmbeddedApplication application

	void 'application is able to start'() {
		expect:
		application.running
	}

}