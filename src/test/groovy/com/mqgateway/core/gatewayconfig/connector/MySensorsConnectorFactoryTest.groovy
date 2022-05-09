package com.mqgateway.core.gatewayconfig.connector

import com.mqgateway.core.io.provider.MySensorsConnector
import com.mqgateway.core.mysensors.InternalType
import com.mqgateway.core.mysensors.PresentationType
import com.mqgateway.core.mysensors.SetReqType
import com.mqgateway.core.mysensors.StreamType
import spock.lang.Specification

class MySensorsConnectorFactoryTest extends Specification {

  def factory = new MySensorsConnectorFactory()

  def "should create connector"(int nodeId, int sensorId, String type, MySensorsConnector connector) {
    given:
    Map<String, Object> config = [
      "nodeId"  : nodeId,
      "sensorId": sensorId,
      "type"    : type
    ]

    expect:
    factory.create(config) == connector

    where:
    nodeId | sensorId | type         || connector
    1      | 1        | "S_DOOR"     || new MySensorsConnector(1, 1, PresentationType.S_DOOR)
    2      | 2        | "I_CHILDREN" || new MySensorsConnector(2, 2, InternalType.I_CHILDREN)
    3      | 3        | "V_ARMED"    || new MySensorsConnector(3, 3, SetReqType.V_ARMED)
    4      | 4        | "STREAM"     || new MySensorsConnector(4, 4, StreamType.STREAM)
  }

  def "should throw exception when type is not supported"() {
    when:
    factory.create([
      "nodeId"  : 1,
      "sensorId": 2,
      "type"    : "SOME_NON_EXISTING_TYPE"
    ])

    then:
    thrown(RuntimeException)
  }
}
