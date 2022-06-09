package com.unigateway.core.hardware.mqgateway.validators

import static com.unigateway.utils.TestGatewayFactory.gateway

import com.unigateway.core.device.DeviceType
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.gatewayconfig.validation.ValidationFailureReason
import com.unigateway.core.hardware.mqgateway.MqGatewayConnector
import com.unigateway.core.hardware.mqgateway.MqGatewayPlatformConfiguration
import com.unigateway.core.hardware.mqgateway.WireColor
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.gatewayconfig.validation.ValidationFailureReason
import com.unigateway.core.hardware.mqgateway.MqGatewayConnector
import com.unigateway.core.hardware.mqgateway.MqGatewayPlatformConfiguration
import com.unigateway.core.hardware.mqgateway.WireColor
import spock.lang.Specification

class PortNumbersRangeValidatorTest extends Specification {

  def "should fail when portNumber is out of range"(int portNumber, boolean expanderEnabled) {
    given:
    PortNumbersRangeValidator validator = new PortNumbersRangeValidator(preparePlatformConfiguration(expanderEnabled))
	GatewayConfiguration config = gateway([
      new DeviceConfiguration("device_1", "Device 1", DeviceType.RELAY, [status: new MqGatewayConnector(portNumber, WireColor.BLUE, 50)])
    ])

    when:
    List<ValidationFailureReason> result = validator.validate(config)

    then:
    result == [new PortNumbersRangeValidator.PortNumberOutOfRange("device_1", "status", portNumber, expanderEnabled)]

    where:
    portNumber || expanderEnabled
    0          || false
    17         || false
    0          || true
    33         || true
  }

  def "should pass when portNumber is in range"(int portNumber, boolean expanderEnabled) {
    given:
    PortNumbersRangeValidator validator = new PortNumbersRangeValidator(preparePlatformConfiguration(expanderEnabled))
    GatewayConfiguration config = gateway([
      new DeviceConfiguration("device_1", "Device 1", DeviceType.RELAY, [status: new MqGatewayConnector(portNumber, WireColor.BLUE, 50)])
    ])

    when:
    List<ValidationFailureReason> result = validator.validate(config)

    then:
    result.isEmpty()

    where:
    portNumber || expanderEnabled
    1          || false
    5          || false
    16         || false
    1          || true
    5          || true
    16         || true
    25         || true
    32         || true
  }

  private MqGatewayPlatformConfiguration preparePlatformConfiguration(boolean expanderEnabled = true, long defaultDebounceMs = 50) {
    MqGatewayPlatformConfiguration.ExpanderConfiguration expanderConfiguration =
      new MqGatewayPlatformConfiguration.ExpanderConfiguration(expanderEnabled)

    return new MqGatewayPlatformConfiguration(
      expanderConfiguration,
      new MqGatewayPlatformConfiguration.ComponentsConfiguration(
        new MqGatewayPlatformConfiguration.ComponentsConfiguration.Mcp23017Configuration(expanderConfiguration, [20, 21, 22, 23])
      ),
      defaultDebounceMs
    )
  }
}
