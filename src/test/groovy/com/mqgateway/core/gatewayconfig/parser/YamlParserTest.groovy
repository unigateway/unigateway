package com.mqgateway.core.gatewayconfig.parser

import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
@Property(name = "gateway.mqtt.enabled", value = "false")
class YamlParserTest extends Specification {

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
