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
import spock.lang.Unroll

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
		""".stripIndent()
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
		 """.stripIndent()
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

  def "should accept 'internalDevices' with three emulated buttons instead of 'wires' when device is of type GATE"() {
    given:
    def device = """
    - id: "garage_gate"
      name: Garage gate
      type: GATE
      config:
        haDeviceClass: gate
      internalDevices:
        stopButton:
          name: "garage gate stop button"
          id: "garage_gate_stop_button"
          type: EMULATED_SWITCH
          wires: ["BLUE"]
        openButton:
          name: "garage gate open button"
          id: "garage_gate_open_button"
          type: EMULATED_SWITCH
          wires: ["BLUE_WHITE"]
        closeButton:
          name: "garage gate close button"
          id: "garage_gate_close_button"
          type: EMULATED_SWITCH
          wires: ["GREEN_WHITE"]
		""".stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = validateAgainstJsonSchema(configWithDevice)

    then:
    validationMessages.isEmpty()
  }

  @Unroll
  def "should fail validation when 'internalDevices' are specified but missing #missingDevice when on device of type GATE"() {
    given:
    def device = """
    - id: "garage_gate"
      name: Garage gate
      type: GATE
      internalDevices:
        $firstDevice:
          name: "garage gate $firstDevice"
          id: "garage_gate_$firstDevice"
          type: EMULATED_SWITCH
          wires: ["BLUE"]
        $secondDevice:
          name: "garage gate $secondDevice"
          id: "garage_gate_$secondDevice"
          type: EMULATED_SWITCH
          wires: ["BLUE_WHITE"]
		""".stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = validateAgainstJsonSchema(configWithDevice)

    then:
    validationMessages.size() == 2
    validationMessages[0].path == "\$.rooms[0].points[0].devices[0].internalDevices"
    validationMessages[0].arguments[0] == missingDevice
    validationMessages[0].type == "required"
    validationMessages[1].path == "\$.rooms[0].points[0].devices[0].internalDevices"
    validationMessages[1].arguments[0] == "actionButton"
    validationMessages[1].type == "required"

    where:
    firstDevice  | secondDevice  | missingDevice
    "stopButton" | "openButton"  | "closeButton"
    "stopButton" | "closeButton" | "openButton"
    "openButton" | "closeButton" | "stopButton"
  }

  def "should fail validation when 'wires' is specified and/or 'internalDevices' is missing for device of type GATE"() {
    given:
    def device = """
     - id: "garage_gate_left"
       name: Left garage gate
       type: GATE
       wires: ["BLUE"]
		 """.stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = validateAgainstJsonSchema(configWithDevice)

    then:
    validationMessages.size() == 1
    validationMessages[0].path == "\$.rooms[0].points[0].devices[0]"
    validationMessages[0].arguments[0] == "internalDevices"
    validationMessages[0].type == "required"
  }

  def "should accept 'internalDevices' with actionButton instead of 'wires' when device is of type GATE"() {
    given:
    def device = """
    - id: "garage_gate"
      name: Garage gate
      type: GATE
      config:
        haDeviceClass: garage
      internalDevices:
        actionButton:
          name: "garage gate button"
          id: "garage_gate_button"
          type: EMULATED_SWITCH
          wires: ["BLUE"]
		""".stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = validateAgainstJsonSchema(configWithDevice)

    then:
    validationMessages.isEmpty()
  }

  def "should fail validation when config specifies haDeviceClass different than 'gate' or 'garage' for device of type GATE"() {
    given:
    def device = """
    - id: "garage_gate"
      name: Garage gate
      type: GATE
      config:
        haDeviceClass: door
      internalDevices:
        actionButton:
          name: "garage gate button"
          id: "garage_gate_button"
          type: EMULATED_SWITCH
          wires: ["BLUE"]
		 """.stripIndent()
    println device
    def configWithDevice = configWithDevice(device)
    checkYamlCorrectness(configWithDevice)

    when:
    Set<ValidationMessage> validationMessages = validateAgainstJsonSchema(configWithDevice)

    then:
    validationMessages.size() == 1
    validationMessages[0].path == "\$.rooms[0].points[0].devices[0].config.haDeviceClass"
    validationMessages[0].arguments[0] == "[gate, garage]"
    validationMessages[0].type == "enum"
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
    name: "TestGateway"
    mqttHostname: "192.168.1.100"
    rooms:
      - name: "Test Room"
        points:
          - name: "Test Point"
            portNumber: 1
            devices:
${device.readLines().collect {"              $it" }.join(System.lineSeparator())}
		""".stripIndent()

    println config
    return config
  }

  private void checkYamlCorrectness(String yamlString) {
    yamlSlurper.parseText(yamlString)
  }
}
