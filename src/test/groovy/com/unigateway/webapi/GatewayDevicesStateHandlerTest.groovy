package com.unigateway.webapi

import spock.lang.Specification
import spock.lang.Subject

class GatewayDevicesStateHandlerTest extends Specification {

  @Subject
  GatewayDevicesStateHandler stateHandler = new GatewayDevicesStateHandler()

  def "should create device state when receiving first update for this device"() {
    given:
    def deviceId = "someNewDevice"
    def propertyId = "state"
    def newValue = "100"

    when:
    stateHandler.valueUpdated(deviceId, propertyId, newValue)

    then:
    stateHandler.devicesState()
      .find {it.deviceId == deviceId}.properties
      .find {it.propertyId }.getValue() == newValue
  }

  def "should update state of device with update is received"() {
    given:
    def deviceId = "someNewDevice"
    def propertyId = "state"
    def newValue = "newValue"
    stateHandler.valueUpdated(deviceId, propertyId, "initialValue")

    when:
    stateHandler.valueUpdated(deviceId, propertyId, newValue)

    then:
    stateHandler.devicesState()
      .find {it.deviceId == deviceId}.properties
      .find {it.propertyId }.getValue() == newValue
  }
}
