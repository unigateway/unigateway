package com.mqgateway.utils

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification

class MqttSpecification extends Specification {

  private static final int MOSQUITTO_INTERNAL_PORT = 1883

  static GenericContainer mosquittoContainer = new GenericContainer(DockerImageName.parse("eclipse-mosquitto:1.6.13"))

  static {
    mosquittoContainer.setPortBindings(["1883:$MOSQUITTO_INTERNAL_PORT".toString()])
    mosquittoContainer.start()
  }

  static int mosquittoPort() {
    return mosquittoContainer.getMappedPort(MOSQUITTO_INTERNAL_PORT)
  }

  static void cleanupMqtt() {
    mosquittoContainer.stop()
    mosquittoContainer.start()
  }
}
