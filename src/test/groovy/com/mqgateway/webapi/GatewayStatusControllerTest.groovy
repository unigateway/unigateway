package com.mqgateway.webapi

import com.mqgateway.configuration.GatewayApplicationProperties
import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.homie.MqttStatusIndicator
import java.time.Duration
import spock.lang.Specification

class GatewayStatusControllerTest extends Specification {

  FakeSystemInfoProvider systemInfoProvider = new FakeSystemInfoProvider()
  MqttStatusIndicator mqttStatusIndicator = new MqttStatusIndicator()
  GatewayApplicationProperties gatewayApplicationProperties = new GatewayApplicationProperties("gateway.yaml", "0.543.0-some-test-version")
  GatewaySystemProperties gatewaySystemProperties = new GatewaySystemProperties("ethAdapter", "SimulatedInTest", [:],
                                                                                new GatewaySystemProperties.MySensors(true, "/some/port", 9876))
  UpdateChecker updateChecker = Mock(UpdateChecker)
  GatewayStatusController gatewayStatusController = new GatewayStatusController(systemInfoProvider, mqttStatusIndicator, gatewayApplicationProperties,
                                                                                gatewaySystemProperties, updateChecker)

  def "should return all information about gateway status"() {
    given:
    systemInfoProvider.cpuTemperature = 7
    systemInfoProvider.ipAddresses = "192.192.192.192"
    systemInfoProvider.uptime = Duration.ofSeconds(1000)
    systemInfoProvider.memoryFree = 500000
    mqttStatusIndicator.onConnected()
    updateChecker.getLatestVersionInfo() >> new ReleaseInfo("test release", "v0.544.0", "https://mqgateway.com/release/v0.544.0")

    when:
    def status = gatewayStatusController.getStatus()

    then:
    status.cpuTemperature == 7
    status.uptimeSeconds == 1000
    status.ipAddress == "192.192.192.192"
    status.freeMemoryBytes == 500000
    status.firmwareVersion == "0.543.0-some-test-version"
    status.mySensorsEnabled
    status.mqttConnected
    status.mqGatewayLatestVersion == new ReleaseInfo("test release", "v0.544.0", "https://mqgateway.com/release/v0.544.0")
  }
}

