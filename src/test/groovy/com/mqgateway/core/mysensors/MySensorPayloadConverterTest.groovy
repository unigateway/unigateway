package com.mqgateway.core.mysensors

import com.mqgateway.core.io.BinaryState
import spock.lang.Specification
import spock.lang.Subject

class MySensorPayloadConverterTest extends Specification {

  @Subject
  MySensorPayloadConverter converter = new MySensorPayloadConverter()

  def "should parse float payload"(String payload, Float value) {
    expect:
    converter.parseFloat(payload) == value

    where:
    payload | value
    "0.0"   | 0f
    "1"     | 1f
    "2.1"   | 2.1f
  }

  def "should serialize float"() {
    expect:
    converter.serializeFloat(value) == payload

    where:
    value | payload
    1f    | "1.0"
    2.1f  | "2.1"
    0.0f  | "0.0"
  }

  def "should parse binary payload"(String payload, BinaryState value) {
    expect:
    converter.parseBinary(payload) == value

    where:
    payload | value
    "ON"    | BinaryState.HIGH
    "OFF"   | BinaryState.LOW
  }

  def "should throw exception when binary payload cannot be parser"() {
    when:
    converter.parseBinary(payload)

    then:
    def e = thrown(Exception)
    e.message == "Binary payload: '$payload' cannot be parsed to binary state"

    where:
    payload << ["STATE_ON", "1", "0", "HIGH", "LOW"]
  }

  def "should serialize binary"() {
    expect:
    converter.serializeBinary(value) == payload

    where:
    value            | payload
    BinaryState.HIGH | "ON"
    BinaryState.LOW  | "OFF"
  }

}
