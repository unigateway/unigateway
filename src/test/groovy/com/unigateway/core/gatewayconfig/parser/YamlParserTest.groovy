package com.unigateway.core.gatewayconfig.parser


import com.unigateway.core.device.DeviceType
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.hardware.simulated.SimulatedConnector
import com.unigateway.utils.MqttSpecification
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(startApplication = false)
class YamlParserTest extends MqttSpecification {

  @Inject
  YamlParser parser

  def "should parse Gateway configuration from YAML file"() {
    given:
    def yamlFileBytes = YamlParserTest.getResourceAsStream("/example.gateway.yaml").bytes

    when:
    GatewayConfiguration gateway = parser.parse(parser.toJsonNode(yamlFileBytes))

    then:
    gateway.configVersion == "1.0"
    gateway.name == "Simulated gateway"

    gateway.devices[0].id == "workshop_light"
    gateway.devices[0].type == DeviceType.RELAY
    gateway.devices[0].name == "Workshop light"
    gateway.devices[0].connectors.state != null
    gateway.devices[0].connectors.state instanceof SimulatedConnector
    (gateway.devices[0].connectors.state as SimulatedConnector).pin == 12
    gateway.devices[0].config.stringValue == "1"
    gateway.devices[0].config.intValue == "300"
  }
}
