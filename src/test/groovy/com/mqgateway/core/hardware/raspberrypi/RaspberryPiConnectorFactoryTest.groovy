package com.mqgateway.core.hardware.raspberrypi

import spock.lang.Specification
import spock.lang.Subject

class RaspberryPiConnectorFactoryTest extends Specification {

  @Subject
  RaspberryPiConnectorFactory factory = new RaspberryPiConnectorFactory()

  def "should create connector"(pin, debounce, pullUpDown) {
    given:
    Map<String, Object> config = [
      "pin"       : pin,
      "debounce"  : debounce,
      "pullUpDown": pullUpDown
    ]

    expect:
    factory.create(config) == expectedConnector

    where:
    pin | debounce | pullUpDown || expectedConnector
    1   | null     | null        | new RaspberryPiConnector(1, null, null)
    2   | 100      | null        | new RaspberryPiConnector(2, 100, null)
    3   | 200      | "PULL_UP"   | new RaspberryPiConnector(3, 200, PullUpDown.PULL_UP)
    4   | 304      | "PULL_DOWN"   | new RaspberryPiConnector(4, 304, PullUpDown.PULL_DOWN)
  }
}
