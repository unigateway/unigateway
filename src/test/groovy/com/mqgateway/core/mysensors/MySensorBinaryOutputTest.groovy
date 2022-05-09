package com.mqgateway.core.mysensors

import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.TestSerial
import com.mqgateway.core.io.provider.MySensorsConnector
import spock.lang.Specification

class MySensorBinaryOutputTest extends Specification {

  TestSerial serial = new TestSerial()

  def serialConnection = new MySensorsSerialConnection(serial, new MySensorMessageSerializer())

  def NODE_ID = 1
  def SENSOR_ID = 2

  def "should send message when changing state to high"() {
    given:
    def connector = new MySensorsConnector(NODE_ID, SENSOR_ID, PresentationType.S_BINARY)
    def output = new MySensorBinaryOutput(serialConnection, connector)

    when:
    output.setState(BinaryState.HIGH)

    then:
    serial.sentMessages == ["${NODE_ID};${SENSOR_ID};${Command.SET.id};0;${PresentationType.S_BINARY.id};ON\n"]
  }

  def "should send message when changing state to low"() {
    given:
    def connector = new MySensorsConnector(NODE_ID, SENSOR_ID, PresentationType.S_BINARY)
    def output = new MySensorBinaryOutput(serialConnection, connector)

    when:
    output.setState(BinaryState.LOW)

    then:
    serial.sentMessages == ["${NODE_ID};${SENSOR_ID};${Command.SET.id};0;${PresentationType.S_BINARY.id};OFF\n"]
  }
}
