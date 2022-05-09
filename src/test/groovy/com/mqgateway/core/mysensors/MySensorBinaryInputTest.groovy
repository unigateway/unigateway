package com.mqgateway.core.mysensors

import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.TestSerial
import com.mqgateway.core.io.provider.MySensorsConnector
import spock.lang.Specification

class MySensorBinaryInputTest extends Specification {

  TestSerial serial = new TestSerial()

  def serialConnection = new MySensorsSerialConnection(serial, new MySensorMessageSerializer())

  def NODE_ID = 1
  def SENSOR_ID = 2

  def "should be notified when state is changed to ON"() {
    given:
    def connector = new MySensorsConnector(NODE_ID,SENSOR_ID, PresentationType.S_BINARY)
    def input = new MySensorBinaryInput(serialConnection, connector)
    def newState = null
    input.addListener((event) -> {
      newState = event.newState()
    })

    when:
    serial.simulateMessageReceived("${NODE_ID};${SENSOR_ID};${Command.PRESENTATION.id};0;${PresentationType.S_BINARY.id};ON\n")

    then:
    newState == BinaryState.HIGH
  }

  def "should be notified when state is changed to OFF"() {
    given:
    def connector = new MySensorsConnector(NODE_ID,SENSOR_ID, PresentationType.S_BINARY)
    def input = new MySensorBinaryInput(serialConnection, connector)
    def newState = null
    input.addListener((event) -> {
      newState = event.newState()
    })

    when:
    serial.simulateMessageReceived("${NODE_ID};${SENSOR_ID};${Command.PRESENTATION.id};0;${PresentationType.S_BINARY.id};OFF\n")

    then:
    newState == BinaryState.LOW
  }

  def "should not be notified when message is for different type"() {
    given:
    def connector = new MySensorsConnector(NODE_ID,SENSOR_ID, PresentationType.S_BINARY)
    def input = new MySensorBinaryInput(serialConnection, connector)
    def newState = null
    input.addListener((event) -> {
      newState = event.newState()
    })

    when:
    serial.simulateMessageReceived("${NODE_ID};${SENSOR_ID};${Command.PRESENTATION.id};0;${PresentationType.S_COLOR_SENSOR.id};ON\n")

    then:
    newState == null
  }

  def "should throw exception when trying to get value"() {
    given:
    def connector = new MySensorsConnector(NODE_ID,SENSOR_ID, InternalType.I_DEBUG)
    def input = new MySensorBinaryInput(serialConnection, connector)

    when:
    input.getState()

    then:
    thrown(UnsupportedOperationException)
  }
}
