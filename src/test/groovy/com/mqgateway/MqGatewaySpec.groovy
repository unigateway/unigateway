package com.mqgateway

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest
class MqGatewaySpec extends Specification {

	@Inject
	EmbeddedApplication application

  PollingConditions conditions = new PollingConditions(timeout: 5)

	void 'application is able to start'() {
		expect:
    conditions.eventually {
      application.running
    }
	}

}