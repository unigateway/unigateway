package com.mqgateway

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import javax.inject.Inject
import spock.lang.Specification
import spock.lang.Timeout

@MicronautTest
class MqGatewaySpec extends Specification {

	@Inject
	EmbeddedApplication application

  @Timeout(30)
	void 'application is able to start'() {
		expect:
    application.running
	}

}