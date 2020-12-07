package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import groovy.yaml.YamlSlurper
import spock.lang.Specification

class JsonSchemaValidationTest extends Specification {

  private static final DEFAULT_CONFIG_VERSION = "1.1"

  YamlSlurper yamlSlurper = new YamlSlurper()
  ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory())

  def "should accept 'internalDevices' and 'config' instead of 'wires' when device is of type SHUTTER"() {
    given:
    def device = """
    - id: "bedroom_shutter"
      name: Bedroom shutter
      type: SHUTTER
      internalDevices:
        stopRelay:
          name: "bedroom shutter stop relay"
          id: "bedroom_shutter_stop_relay"
          type: RELAY
          wires: ["BLUE"]
        upDownRelay:
          name: "bedroom shutter up-down relay"
          id: "bedroom_shutter_updown_relay"
          type: RELAY
          wires: ["BLUE_WHITE"]
      config:
        fullCloseTimeMs: 14000
        fullOpenTimeMs: 17000
		""".stripIndent().stripLeading()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = validateAgainstJsonSchema(configWithDevice)

    then:
    validationMessages.isEmpty()
  }

  def "should fail validation when 'wires' is specified and/or 'internalDevices' is missing for device of type SHUTTER"() {
    given:
    def device = """
     - id: "bedroom_shutter"
       name: Bedroom shutter
       type: SHUTTER
       wires: ["BLUE"]
		 """.stripIndent().stripLeading()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = validateAgainstJsonSchema(configWithDevice)

    then:
    validationMessages.size() == 2
    validationMessages[0].path == "\$.rooms[0].points[0].devices[0]"
    validationMessages[0].arguments[0] == "internalDevices"
    validationMessages[0].type == "required"
    validationMessages[1].path == "\$.rooms[0].points[0].devices[0]"
    validationMessages[1].arguments[0] == "config"
    validationMessages[1].type == "required"
  }

  Set<ValidationMessage> validateAgainstJsonSchema(String yaml) {
    JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).objectMapper(objectMapper).build()
    JsonSchema schema = factory.getSchema(ConfigValidator.getClassLoader().getResourceAsStream("config.schema.json"))

    JsonNode jsonNode = objectMapper.readTree(yaml)
    return schema.validate(jsonNode);
  }

  private static String configWithDevice(String device, String configVersion = DEFAULT_CONFIG_VERSION) {
    def config = """
    configVersion: "$configVersion"
    name: "TestGateway"
    mqttHostname: "192.168.1.100"
    rooms:
      - name: "Test Room"
        points:
          - name: "Test Point"
            portNumber: 1
            devices:
${device.lines().collect {"              $it" }.join(System.lineSeparator())}
		""".stripIndent()

    println config
    return config
  }

  private void checkYamlCorrectness(String yamlString) {
    yamlSlurper.parseText(yamlString)
  }
}
