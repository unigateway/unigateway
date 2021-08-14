package com.mqgateway.mysensors

import spock.lang.Specification
import spock.lang.Subject

class MySensorMessageParserTest extends Specification {

  @Subject
  MySensorMessageParser parser = new MySensorMessageParser()

  def "should parse message from string"() {
    given:
    String messageString = "123;43;3;0;28;This is debug message"

    when:
    def message = parser.parse(messageString)

    then:
    message.nodeId == 123
    message.childSensorId == 43
    message.command == Command.INTERNAL
    !message.ack
    message.type == InternalType.I_DEBUG
    message.payload == "This is debug message"

  }

  def "should fail with exception when command in message string is unknown"() {
    given:
    String messageString = "123;43;5;0;28;This is debug message"

    when:
    parser.parse(messageString)

    then:
    thrown(UnknownCommandException)
  }

  def "should fail with exception when command type in message string is unknown"() {
    given:
    String messageString = "123;43;3;0;57;This is debug message"

    when:
    parser.parse(messageString)

    then:
    thrown(UnknownCommandTypeException)
  }

  def "should serialize message from object"() {
    given:
    def message = new Message(23, 1, Command.SET, false, SetReqType.V_TEMP, "18.4")

    when:
    def serializedMessage = parser.serialize(message)

    then:
    serializedMessage == "23;1;1;0;0;18.4\n"
  }

  def "should parse message without payload"() {
    given:
    String messageString = "123;43;3;0;28;"

    when:
    def message = parser.parse(messageString)

    then:
    message.nodeId == 123
    message.childSensorId == 43
    message.command == Command.INTERNAL
    !message.ack
    message.type == InternalType.I_DEBUG
    message.payload == ""
  }
}
