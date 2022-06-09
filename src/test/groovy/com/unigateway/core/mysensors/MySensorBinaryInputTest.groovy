package com.unigateway.core.mysensors

import com.unigateway.core.io.BinaryState
import com.unigateway.core.io.TestSerial
import com.unigateway.core.io.provider.MySensorsConnector
import com.unigateway.core.io.TestSerial
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
    serial.simulateMessageReceived("${NODE_ID};${SENSOR_ID};${Command.PRESENTATION.id};0;${PresentationType.S_BINARY.id};1\n")

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
    serial.simulateMessageReceived("${NODE_ID};${SENSOR_ID};${Command.PRESENTATION.id};0;${PresentationType.S_BINARY.id};0\n")

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

  def "should getState return LOW state"() {
    given:
    def connector = new MySensorsConnector(NODE_ID,SENSOR_ID, InternalType.I_DEBUG)
    def input = new MySensorBinaryInput(serialConnection, connector)

    expect:
    input.getState() == BinaryState.LOW
  }
}
