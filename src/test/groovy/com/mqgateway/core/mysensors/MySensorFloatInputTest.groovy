package com.mqgateway.core.mysensors

import com.mqgateway.core.io.TestSerial
import com.mqgateway.core.io.provider.MySensorsConnector
import spock.lang.Specification

class MySensorFloatInputTest extends Specification {

  TestSerial serial = new TestSerial()

  def serialConnection = new MySensorsSerialConnection(serial, new MySensorMessageSerializer())

  def NODE_ID = 1
  def SENSOR_ID = 2

  def "should be notified when state is changed"(String valueInMessage) {
    given:
    def connector = new MySensorsConnector(NODE_ID, SENSOR_ID, PresentationType.S_TEMP)
    def input = new MySensorFloatInput(serialConnection, connector)
    def newValue = null
    input.addListener((event) -> {
      newValue = event.newValue()
    })

    when:
    serial.simulateMessageReceived("${NODE_ID};${SENSOR_ID};${Command.PRESENTATION.id};0;${PresentationType.S_TEMP.id};${valueInMessage}\n")

    then:
    newValue == valueInMessage.toFloat()

    where:
    valueInMessage << ["0", "1.0", "36.6"]
  }

  def "should not be notified when message is for different type"() {
    given:
    def connector = new MySensorsConnector(NODE_ID, SENSOR_ID, PresentationType.S_TEMP)
    def input = new MySensorFloatInput(serialConnection, connector)
    def newValue = null
    input.addListener((event) -> {
      newValue = event.newValue()
    })

    when:
    serial.simulateMessageReceived("${NODE_ID};${SENSOR_ID};${Command.PRESENTATION.id};0;${PresentationType.S_DISTANCE.id};36.6\n")

    then:
    newValue == null
  }

  def "should throw exception when trying to get value"() {
    given:
    def connector = new MySensorsConnector(NODE_ID, SENSOR_ID, InternalType.I_DEBUG)
    def input = new MySensorFloatInput(serialConnection, connector)

    when:
    input.getValue()

    then:
    thrown(UnsupportedOperationException)
  }

}
