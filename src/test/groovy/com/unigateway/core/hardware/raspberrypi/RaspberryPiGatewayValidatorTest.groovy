package com.unigateway.core.hardware.raspberrypi

import static com.unigateway.utils.TestGatewayFactory.gateway

import com.unigateway.core.device.DeviceType
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.hardware.raspberrypi.validators.UniqueGpioNumbersValidatorGatewayValidator
import com.unigateway.core.device.DeviceType
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.hardware.raspberrypi.validators.UniqueGpioNumbersValidatorGatewayValidator
import kotlin.Pair
import spock.lang.Specification
import spock.lang.Subject

class RaspberryPiGatewayValidatorTest extends Specification {

  @Subject
  RaspberryPiGatewayValidator validator = new RaspberryPiGatewayValidator()

  def "should validate gpio number uniqueness"() {
    given:
    def config = gateway([new DeviceConfiguration("device_1", "Device 1", DeviceType.RELAY, [
      status: new RaspberryPiConnector(1, 0)
    ]), new DeviceConfiguration("device_2", "Device 2", DeviceType.RELAY, [
      status: new RaspberryPiConnector(2, 0)
    ]), new DeviceConfiguration("device_3", "Device 3", DeviceType.RELAY, [
      connector_name: new RaspberryPiConnector(1, 10)
    ]), new DeviceConfiguration("device_4", "Device 4", DeviceType.RELAY, [
      status: new RaspberryPiConnector(2, 0)
    ]), new DeviceConfiguration("device_5", "Device 5", DeviceType.RELAY, [
      status: new RaspberryPiConnector(3, 0)
    ])])

    expect:
    validator.validate(config) == [
      new UniqueGpioNumbersValidatorGatewayValidator.GpioNumberNotUnique(1, [
        new Pair("device_1", "status"),
        new Pair("device_3", "connector_name")
      ]), new UniqueGpioNumbersValidatorGatewayValidator.GpioNumberNotUnique(2, [
      new Pair("device_2", "status"),
      new Pair("device_4", "status")
    ])
    ]
  }
}
