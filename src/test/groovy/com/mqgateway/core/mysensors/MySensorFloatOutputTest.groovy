package com.mqgateway.core.mysensors

import com.mqgateway.core.io.TestSerial
import com.mqgateway.core.io.provider.MySensorsConnector
import spock.lang.Specification

class MySensorFloatOutputTest extends Specification {

  TestSerial serial = new TestSerial()

  def serialConnection = new MySensorsSerialConnection(serial, new MySensorMessageSerializer())

  def NODE_ID = 1
  def SENSOR_ID = 2

  def "should send message when changing state"() {
    given:
    def connector = new MySensorsConnector(NODE_ID, SENSOR_ID, PresentationType.S_TEMP)
    def output = new MySensorFloatOutput(serialConnection, connector)

    when:
    output.setValue(30.1)

    then:
    serial.sentMessages == ["${NODE_ID};${SENSOR_ID};${Command.SET.id};0;${PresentationType.S_TEMP.id};30.1\n"]
  }

}
