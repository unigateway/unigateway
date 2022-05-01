package com.mqgateway.core.hardware.mqgateway.validators

import static com.mqgateway.utils.TestGatewayFactory.gateway

import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import com.mqgateway.core.hardware.mqgateway.MqGatewayConnector
import com.mqgateway.core.hardware.mqgateway.WireColor
import kotlin.Pair
import spock.lang.Specification
import spock.lang.Subject

class UniqueWiresValidatorGatewayValidatorTest extends Specification {

  @Subject
  UniqueWiresValidatorGatewayValidator validator = new UniqueWiresValidatorGatewayValidator()

  def "should pass validation when all devices uses unique wires"() {
    given:
    GatewayConfiguration config = gateway([
      new DeviceConfiguration("device_1", "Device 1", DeviceType.RELAY, [status: new MqGatewayConnector(1, WireColor.BLUE, 50)]),
      new DeviceConfiguration("device_2", "Device 2", DeviceType.RELAY, [status: new MqGatewayConnector(1, WireColor.BLUE_WHITE, 50)]),
      new DeviceConfiguration("device_3", "Device 3", DeviceType.RELAY, [status: new MqGatewayConnector(2, WireColor.BLUE, 50)]),
    ])

    when:
    List<ValidationFailureReason> result = validator.validate(config)

    then:
    result.isEmpty()
  }

  def "should fail validation when two or more devices uses the same wire"() {
    given:
    GatewayConfiguration config = gateway([
      new DeviceConfiguration("device_1", "Device 1", DeviceType.RELAY, [status: new MqGatewayConnector(1, WireColor.BLUE, 50)]),
      new DeviceConfiguration("device_2", "Device 2", DeviceType.RELAY, [status: new MqGatewayConnector(2, WireColor.BLUE_WHITE, 50)]),
      new DeviceConfiguration("device_3", "Device 3", DeviceType.RELAY, [status: new MqGatewayConnector(1, WireColor.BLUE, 50)]),
      new DeviceConfiguration("device_4", "Device 4", DeviceType.RELAY, [status: new MqGatewayConnector(2, WireColor.BLUE_WHITE, 50)]),
      new DeviceConfiguration("device_5", "Device 5", DeviceType.RELAY, [status: new MqGatewayConnector(2, WireColor.BLUE_WHITE, 50)]),
    ])

    when:
    List<ValidationFailureReason> result = validator.validate(config)

    then:
    result.toSet() == [
      new UniqueWiresValidatorGatewayValidator.MqGatewayWiresNotUnique(1, WireColor.BLUE, [
        new Pair<String, String>("device_1", "status"),
        new Pair<String, String>("device_3", "status")
      ]),
      new UniqueWiresValidatorGatewayValidator.MqGatewayWiresNotUnique(2, WireColor.BLUE_WHITE, [
        new Pair<String, String>("device_2", "status"),
        new Pair<String, String>("device_4", "status"),
        new Pair<String, String>("device_5", "status")
      ])
    ].toSet()
  }
}
