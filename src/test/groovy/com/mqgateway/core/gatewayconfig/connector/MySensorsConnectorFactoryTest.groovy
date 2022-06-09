package com.mqgateway.core.gatewayconfig.connector

import com.mqgateway.core.io.provider.MySensorsConnector
import com.mqgateway.core.mysensors.SetReqType
import spock.lang.Specification

class MySensorsConnectorFactoryTest extends Specification {

  def factory = new MySensorsConnectorFactory()

  def "should create connector"() {
    given:
    Map<String, Object> config = [
      "nodeId"  : 3,
      "sensorId": 2,
      "type"    : "V_ARMED"
    ]

    expect:
    factory.create(config) == new MySensorsConnector(3, 2, SetReqType.V_ARMED)
  }

  def "should throw exception when type is not supported"(String type) {
    when:
    factory.create([
      "nodeId"  : 1,
      "sensorId": 2,
      "type"    : type
    ])

    then:
    thrown(MySensorsEventTypeNotSupported)

    where:
    type << ["S_DOOR", "I_CHILDREN", "STREAM", "SOME_NON_EXISTING_TYPE"]
  }
}
