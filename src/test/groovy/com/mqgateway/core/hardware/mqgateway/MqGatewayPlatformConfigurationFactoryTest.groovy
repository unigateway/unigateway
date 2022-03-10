package com.mqgateway.core.hardware.mqgateway

import spock.lang.Specification
import spock.lang.Subject

class MqGatewayPlatformConfigurationFactoryTest extends Specification {

  @Subject
  MqGatewayPlatformConfigurationFactory factory = new MqGatewayPlatformConfigurationFactory()

  def "should create MqGateway platform configuration from Map"() {
    given:
    Map configMap = [expander  : [enabled: true],
                     components: [mcp23017: [ports: ["12", "13", "14", "15"]]],
                     'default-debounce-ms': "85"]

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    platformConfiguration.expander.enabled
    platformConfiguration.components.mcp23017.ports == ["12", "13", "14", "15"].collect {Integer.parseInt(it, 16)}
    platformConfiguration.defaultDebounceMs == 85
  }

  def "should set components ports to default when not set explicitly and expander is #expanderState"(String expanderState, boolean expanderEnabled) {
    given:
    Map configMap = [expander  : [enabled: expanderEnabled]]

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    platformConfiguration.components.mcp23017.ports == mcp23017Ports.collect {Integer.parseInt(it, 16)}

    where:
    expanderState | expanderEnabled | mcp23017Ports
    "enabled"     | true            | ["20", "21", "22", "23", "24", "25", "26", "27"]
    "disabled"    | false           | ["20", "21", "22", "23"]
  }

  def "should disable expander in configuration when not set to true explicitly"() {
    given:
    Map configMap = [:]

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    !platformConfiguration.expander.enabled
  }

  def "should set default-debounce-ms to 50 when it is not set explicitly"() {
    given:
    Map configMap = [:]

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    platformConfiguration.defaultDebounceMs == 50

  }
}
