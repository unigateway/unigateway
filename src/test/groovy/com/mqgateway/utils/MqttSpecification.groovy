package com.mqgateway.utils

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import spock.lang.Specification

class MqttSpecification extends Specification {

  private static final int MOSQUITTO_INTERNAL_PORT = 1883

  static GenericContainer mosquittoContainer =
    new GenericContainer(DockerImageName.parse("eclipse-mosquitto:2.0.21"))
      .waitingFor(Wait.forLogMessage(/.* mosquitto .* running\n/, 1))

  static {
    mosquittoContainer.setPortBindings(["1883:$MOSQUITTO_INTERNAL_PORT".toString()])
    def mosquittoConfigFilePath = new File(MqttSpecification.class.getClassLoader().getResource("mosquitto.conf").toURI()).absolutePath
    def mosquittoPasswordFilePath = new File(MqttSpecification.class.getClassLoader().getResource("mosquitto_password").toURI()).absolutePath
    mosquittoContainer.withCopyFileToContainer(MountableFile.forHostPath(mosquittoConfigFilePath), "/mosquitto/config/mosquitto.conf")
    mosquittoContainer.withCopyFileToContainer(MountableFile.forHostPath(mosquittoPasswordFilePath), "/mosquitto/config/mosquitto_password")
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
