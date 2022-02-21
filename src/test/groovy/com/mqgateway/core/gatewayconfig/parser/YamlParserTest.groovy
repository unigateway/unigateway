package com.mqgateway.core.gatewayconfig.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.connector.ConnectorFactory
import com.mqgateway.core.gatewayconfig.connector.MySensorsConnectorFactory
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedConnectorFactory
import spock.lang.Specification

class YamlParserTest extends Specification {

  YamlParser parser

  void setup() {
    def objectMapper = new ObjectMapper(new YAMLFactory())
    objectMapper.registerModule(new KotlinModule())
    ConnectorFactory<SimulatedConnector> connectorFactory = new ConnectorFactory(new MySensorsConnectorFactory(), new SimulatedConnectorFactory())
    objectMapper.registerModule(new ConfigurationJacksonModule(connectorFactory))
    parser = new YamlParser(objectMapper)
  }

  def "should parse Gateway configuration from YAML file"() {
    given:
    def yamlFileBytes = YamlParserTest.getResourceAsStream("/example-1.0-simulated.gateway.yaml").bytes

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
    (gateway.devices[0].connectors.state as SimulatedConnector).pin == 1
    gateway.devices[0].config.stringValue == "1"
    gateway.devices[0].config.intValue == "300"
  }

}
