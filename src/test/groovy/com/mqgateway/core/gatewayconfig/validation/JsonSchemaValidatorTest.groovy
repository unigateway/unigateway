package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.mqgateway.configuration.GatewaySystemProperties
import com.networknt.schema.ValidationMessage
import com.networknt.schema.ValidatorTypeCode
import groovy.yaml.YamlSlurper
import spock.lang.Specification
import spock.lang.Subject

class JsonSchemaValidatorTest extends Specification {

  private static final DEFAULT_CONFIG_VERSION = "1.0"

  YamlSlurper yamlSlurper = new YamlSlurper()
  ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory())
  def simulatedSystemProperties = new GatewaySystemProperties("eth0", "SIMULATED", [:],
                                                              new GatewaySystemProperties.MySensors(false, "", 100))
  def raspberryPiSystemProperties = new GatewaySystemProperties("eth0", "RASPBERRYPI", [:],
                                                                new GatewaySystemProperties.MySensors(false, "", 100))
  def mqGatewaySystemProperties = new GatewaySystemProperties("eth0", "MQGATEWAY", [:],
                                                              new GatewaySystemProperties.MySensors(false, "", 100))

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
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages.isEmpty()
  }

  def "should not allow for additional properties on simulated connector"() {
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
          theTruthIsThat: "js is better than kotlin"
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages == [ValidationMessage.of(
      ValidatorTypeCode.ADDITIONAL_PROPERTIES.value,
      ValidatorTypeCode.ADDITIONAL_PROPERTIES,
      "\$.devices[0].connectors.status",
      "theTruthIsThat")].toSet()
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
          gpio: 1
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages.isEmpty()
  }

  def "should not allow for additional properties on raspberry pi connector"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, raspberryPiSystemProperties)
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          gpio: 1
          theTruthIsThat: "js is better than kotlin"
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages == [ValidationMessage.of(
      ValidatorTypeCode.ADDITIONAL_PROPERTIES.value,
      ValidatorTypeCode.ADDITIONAL_PROPERTIES,
      "\$.devices[0].connectors.status",
      "theTruthIsThat")].toSet()
  }

  def "should validate MqGateway connector"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, mqGatewaySystemProperties)
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          portNumber: 10
          wireColor: BLUE_WHITE
          debounceMs: 70
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages.isEmpty()
  }

  def "should not allow for additional properties on MqGateway connector"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, mqGatewaySystemProperties)
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          portNumber: 1
          wireColor: GREEN
          theTruthIsThat: "sentence about js causes validation failure"
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages == [ValidationMessage.of(
      ValidatorTypeCode.ADDITIONAL_PROPERTIES.value,
      ValidatorTypeCode.ADDITIONAL_PROPERTIES,
      "\$.devices[0].connectors.status",
      "theTruthIsThat")].toSet()
  }

  def "should not allow values outside of [1-32] for portNumber on MqGateway connector"(int portNumber, ValidatorTypeCode validatorTypeCode, int limitNumber) {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, mqGatewaySystemProperties)
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          portNumber: $portNumber
          wireColor: BLUE
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages == [ValidationMessage.of(
      validatorTypeCode.value,
      validatorTypeCode,
      "\$.devices[0].connectors.status.portNumber",
      limitNumber.toString())].toSet()

    where:
    portNumber | validatorTypeCode         | limitNumber
    -1         | ValidatorTypeCode.MINIMUM | 1
    0          | ValidatorTypeCode.MINIMUM | 1
    33         | ValidatorTypeCode.MAXIMUM | 32
  }

  def "should not allow incorrect values for wireColor on MqGateway connector"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, mqGatewaySystemProperties)
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          portNumber: 1
          wireColor: WHITE
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages == [ValidationMessage.of(
      ValidatorTypeCode.ENUM.value,
      ValidatorTypeCode.ENUM,
      "\$.devices[0].connectors.status.wireColor",
      "[BLUE, BLUE_WHITE, GREEN, GREEN_WHITE]")].toSet()
  }

  def "should not allow negative numbers as value for debounceMs on MqGateway connector"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, mqGatewaySystemProperties)
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          portNumber: 1
          wireColor: GREEN
          debounceMs: -1
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    validationMessages == [ValidationMessage.of(
      ValidatorTypeCode.MINIMUM.value,
      ValidatorTypeCode.MINIMUM,
      "\$.devices[0].connectors.status.debounceMs",
      "0")].toSet()
  }

  def "should not validate connector when json schema does not exist for hardware"() {
    given:
    jsonSchemaValidator = new JsonSchemaValidator(objectMapper, new GatewaySystemProperties(
      "eth0", "FAKE-NOT-SUPPORTED-PLATFORM", [:], new GatewaySystemProperties.MySensors(false, "", 100)))
    def device = """
    - id: "example_device"
      name: Example
      type: RELAY
      connectors:
        status:
          some-fake-connector: 1
          it-does-not-matter: "what is here"
		""".stripIndent()
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
    return config
  }

  private void checkYamlCorrectness(String yamlString) {
    yamlSlurper.parseText(yamlString)
  }
}
