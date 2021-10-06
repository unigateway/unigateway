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
  GatewaySystemProperties gatewaySystemProperties = prepareSystemProperties()
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
    !status.expanderEnabled
    status.firmwareVersion == "0.543.0-some-test-version"
    status.mqttConnected
    status.mySensorsEnabled
    status.mqGatewayLatestVersion == new ReleaseInfo("test release", "v0.544.0", "https://mqgateway.com/release/v0.544.0")
  }

  static GatewaySystemProperties prepareSystemProperties(GatewaySystemProperties.ExpanderConfiguration expanderConfiguration = null,
                                                         GatewaySystemProperties.ComponentsConfiguration.Mcp23017Configuration mcp23017Configuration = null,
                                                         GatewaySystemProperties.ComponentsConfiguration.MySensors mySensors = null) {

    def defaultExpanderConfiguration = new GatewaySystemProperties.ExpanderConfiguration(false)
    def defaultMcp23017Configuration = new GatewaySystemProperties.ComponentsConfiguration.Mcp23017Configuration(expanderConfiguration ?: defaultExpanderConfiguration, null)
    def mySensorsDefaultConfiguration = new GatewaySystemProperties.ComponentsConfiguration.MySensors(true, "/dev/myserial")

    def componentsConfiguration = new GatewaySystemProperties.ComponentsConfiguration(mcp23017Configuration ?: defaultMcp23017Configuration,
                                                                                      mySensors ?: mySensorsDefaultConfiguration)
    return new GatewaySystemProperties("eth0",
                                       GatewaySystemProperties.SystemPlatform.SIMULATED,
                                       expanderConfiguration ?: defaultExpanderConfiguration,
                                       componentsConfiguration)
  }
}

