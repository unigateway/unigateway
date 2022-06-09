package com.unigateway.core.hardware.simulated

import spock.lang.Specification
import spock.lang.Subject

class SimulatedPlatformConfigurationFactoryTest extends Specification {

  @Subject
  SimulatedPlatformConfigurationFactory factory = new SimulatedPlatformConfigurationFactory()

  def "should create simulated platform configuration from Map"() {
    given:
    Map configMap = ["some-config": "testValue"]

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    platformConfiguration.someConfig == "testValue"
  }

  def "should create simulated platform configuration with default values when not given values explicitly"() {
    given:
    Map configMap = [:]

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    platformConfiguration.someConfig == "default"
  }
}
