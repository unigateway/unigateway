package com.mqgateway.core.hardware.raspberrypi


import spock.lang.Specification
import spock.lang.Subject

class RaspberryPiPlatformConfigurationFactoryTest extends Specification {

  @Subject
  RaspberryPiPlatformConfigurationFactory factory = new RaspberryPiPlatformConfigurationFactory()

  def "should create MqGateway platform configuration from Map"() {
    given:
    Map configMap = ['default-pull-up-down': 'PULL_DOWN',
                     'default-debounce-ms': '70']

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    platformConfiguration.defaultPullUpDown == PullUpDown.PULL_DOWN
    platformConfiguration.defaultDebounceMs == 70
  }

  def "should set default-debounce-ms to 50 when it is not set explicitly"() {
    given:
    Map configMap = [:]

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    platformConfiguration.defaultDebounceMs == 50

  }

  def "should set default-pull-up-down to PULL_UP when it is not set explicitly"() {
    given:
    Map configMap = [:]

    when:
    def platformConfiguration = factory.create(configMap)

    then:
    platformConfiguration.defaultPullUpDown == PullUpDown.PULL_UP

  }
}
