package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.mqgateway.configuration.GatewaySystemProperties
import com.networknt.schema.ValidationMessage
import groovy.yaml.YamlSlurper
import spock.lang.Specification
import spock.lang.Subject

class JsonSchemaValidatorTest extends Specification {

  private static final DEFAULT_CONFIG_VERSION = "1.0"

  YamlSlurper yamlSlurper = new YamlSlurper()
  ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory())
  def simulatedSystemProperties = new GatewaySystemProperties("eth0", "SIMULATED", [:], "localhost")
  def raspberryPiSystemProperties = new GatewaySystemProperties("eth0", "RASPBERRYPI", [:], "localhost")

  @Subject
  JsonSchemaValidator jsonSchemaValidator

  def "should validate simulated connector based on hardware type"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, simulatedSystemProperties)
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          source: HARDWARE
          pin: 1
		""".stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages.isEmpty()
  }

  def "should validate raspberry pi connector"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, raspberryPiSystemProperties)
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          pin: 1
		""".stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages.isEmpty()
  }

  def "should not validate connector when json schema does not exist for hardware"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, new GatewaySystemProperties(
      "eth0", "FAKE-NOT-SUPPORTED-PLATFORM", [:], "localhost"))
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          some-fake-connector: 1
          it-does-not-matter: "what is here"
		""".stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages.isEmpty()
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
