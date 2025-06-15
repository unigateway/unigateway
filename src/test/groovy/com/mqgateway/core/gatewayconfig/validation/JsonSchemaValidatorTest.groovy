package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.mqgateway.configuration.GatewaySystemProperties
import com.networknt.schema.JsonNodePath
import com.networknt.schema.ValidationMessage
import com.networknt.schema.ValidatorTypeCode
import groovy.yaml.YamlSlurper
import spock.lang.Specification
import spock.lang.Subject

import static com.networknt.schema.PathType.JSON_PATH

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
          theTruthIsThat: "js is better than kotlin for very specific use cases"
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)
    JsonNodePath expectedFailingPath = new JsonNodePath(JSON_PATH).append("devices").append(0).append("connectors").append("status")

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    assertValidationMessages(validationMessages, [
      new ExpectedValidationMessage(ValidatorTypeCode.ADDITIONAL_PROPERTIES, expectedFailingPath, "theTruthIsThat")
    ])
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
          theTruthIsThat: "it depends"
		""".stripIndent()
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)
    JsonNode jsonNode = objectMapper.readTree(configWithDevice)
    JsonNodePath expectedFailingPath = new JsonNodePath(JSON_PATH).append("devices").append(0).append("connectors").append("status")

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    assertValidationMessages(validationMessages, [
      new ExpectedValidationMessage(ValidatorTypeCode.ADDITIONAL_PROPERTIES, expectedFailingPath, "theTruthIsThat")
    ])
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
    JsonNodePath expectedFailingPath = new JsonNodePath(JSON_PATH).append("devices").append(0).append("connectors").append("status")

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    assertValidationMessages(validationMessages, [
      new ExpectedValidationMessage(ValidatorTypeCode.ADDITIONAL_PROPERTIES, expectedFailingPath, "theTruthIsThat")
    ])
  }

  def "should not allow values outside of [1-32] for portNumber on MqGateway connector"(int portNumber, ValidatorTypeCode validatorTypeCode) {
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
    JsonNodePath expectedFailingPath =
      new JsonNodePath(JSON_PATH).append("devices").append(0).append("connectors").append("status").append("portNumber")

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    assertValidationMessages(validationMessages, [
      new ExpectedValidationMessage(validatorTypeCode, expectedFailingPath)
    ])

    where:
    portNumber | validatorTypeCode
    -1         | ValidatorTypeCode.MINIMUM
    0          | ValidatorTypeCode.MINIMUM
    33         | ValidatorTypeCode.MAXIMUM
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
    JsonNodePath expectedFailingPath =
      new JsonNodePath(JSON_PATH).append("devices").append(0).append("connectors").append("status").append("wireColor")

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    assertValidationMessages(validationMessages, [
      new ExpectedValidationMessage(ValidatorTypeCode.ENUM, expectedFailingPath)
    ])
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
    JsonNodePath expectedFailingPath =
      new JsonNodePath(JSON_PATH).append("devices").append(0).append("connectors").append("status").append("debounceMs")

    when:
    Set<ValidationMessage> validationMessages = jsonSchemaValidator.validate(jsonNode)

    then:
    assertValidationMessages(validationMessages, [
      new ExpectedValidationMessage(ValidatorTypeCode.MINIMUM, expectedFailingPath)
    ])
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

  private void assertValidationMessages(Set<ValidationMessage> actual, Collection<ExpectedValidationMessage> expected) {
    def actualToExpectedFormat= actual.collect {
      new ExpectedValidationMessage(
        ValidatorTypeCode.fromValue(it.type),
        it.instanceLocation,
        it.property,
      )
    }

    assert actualToExpectedFormat.toSet() == expected.toSet()
  }

  class ExpectedValidationMessage {
    ValidatorTypeCode type
    JsonNodePath instanceLocation
    String property

    ExpectedValidationMessage(ValidatorTypeCode type, JsonNodePath instanceLocation, String property = null) {
      this.type = type
      this.instanceLocation = instanceLocation
      this.property = property
    }

    boolean equals(o) {
      if (this.is(o)) return true
      if (o == null || getClass() != o.class) return false

      ExpectedValidationMessage that = (ExpectedValidationMessage) o

      if (instanceLocation != that.instanceLocation) return false
      if (property != that.property) return false
      if (type != that.type) return false

      return true
    }

    int hashCode() {
      int result
      result = (type != null ? type.hashCode() : 0)
      result = 31 * result + (instanceLocation != null ? instanceLocation.hashCode() : 0)
      result = 31 * result + (property != null ? property.hashCode() : 0)
      return result
    }


    @Override
    String toString() {
      return "ExpectedValidationMessage{" +
        "type=" + type +
        ", instanceLocation=" + instanceLocation +
        ", property='" + property + '\'' +
        '}';
    }
  }


}
