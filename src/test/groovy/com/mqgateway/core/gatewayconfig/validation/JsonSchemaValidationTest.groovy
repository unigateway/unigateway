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

  private static final DEFAULT_CONFIG_VERSION = "1.0"

  YamlSlurper yamlSlurper = new YamlSlurper()
  ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory())

  def "should accept more properties on connector"() {
    given:
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          source: HARDWARE
          pin: 1
          someSpecifiedProperty: "123"
          objectProperty: 
            prop1: "12"
            prop2: "21"
		""".stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = validateAgainstJsonSchema(configWithDevice)

    then:
    validationMessages.isEmpty()
  }

  Set<ValidationMessage> validateAgainstJsonSchema(String yaml) {
    JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).objectMapper(objectMapper).build()
    JsonSchema schema = factory.getSchema(ConfigValidator.getClassLoader().getResourceAsStream("config.schema.json"))

    JsonNode jsonNode = objectMapper.readTree(yaml)
    return schema.validate(jsonNode)
  }

  private static String configWithDevice(String device, String configVersion = DEFAULT_CONFIG_VERSION) {
    def config = """
    configVersion: "$configVersion"
    id: "unigateway_id"
    name: "TestGateway"
    devices:
${device.readLines().collect { "    $it" }.join(System.lineSeparator())}
		""".stripIndent()

    println config
    return config
  }

  private void checkYamlCorrectness(String yamlString) {
    yamlSlurper.parseText(yamlString)
  }
}
