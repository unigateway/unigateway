package com.unigateway.core.mysensors

import com.unigateway.core.io.BinaryState
import spock.lang.Specification

class MySensorPayloadConverterTest extends Specification {

  def "should parse float payload"(String payload, Float value) {
    expect:
    MySensorPayloadConverter.parseFloat(payload) == value

    where:
    payload | value
    "0.0"   | 0f
    "1"     | 1f
    "2.1"   | 2.1f
  }

  def "should serialize float"() {
    expect:
    MySensorPayloadConverter.serializeFloat(value) == payload

    where:
    value | payload
    1f    | "1.0"
    2.1f  | "2.1"
    0.0f  | "0.0"
  }

  def "should parse binary payload"(String payload, BinaryState value) {
    expect:
    MySensorPayloadConverter.parseBinary(payload) == value

    where:
    payload | value
    "1"     | BinaryState.HIGH
    "0"     | BinaryState.LOW
  }

  def "should throw exception when binary payload cannot be parser"() {
    when:
    MySensorPayloadConverter.parseBinary(payload)

    then:
    def e = thrown(Exception)
    e.message == "Binary payload: '$payload' cannot be parsed to binary state"

    where:
    payload << ["STATE_ON", "ON", "OFF", "HIGH", "LOW"]
  }

  def "should serialize binary"() {
    expect:
    MySensorPayloadConverter.serializeBinary(value) == payload

    where:
    value            | payload
    BinaryState.HIGH | "1"
    BinaryState.LOW  | "0"
  }

}
