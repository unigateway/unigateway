package com.mqgateway.core.hardware.mqgateway

import spock.lang.Specification
import spock.lang.Subject

class MqGatewayConnectorFactoryTest extends Specification {

  @Subject
  MqGatewayConnectorFactory connectorFactory = new MqGatewayConnectorFactory()

  def "should build connector from given data"() {
    given:
    Map connectorMap = [portNumber: 3, wireColor: "GREEN_WHITE", debounceMs: 11]

    when:
    MqGatewayConnector connector = connectorFactory.create(connectorMap)

    then:
    connector.portNumber == 3
    connector.wireColor == WireColor.GREEN_WHITE
    connector.debounceMs == 11
  }
}
