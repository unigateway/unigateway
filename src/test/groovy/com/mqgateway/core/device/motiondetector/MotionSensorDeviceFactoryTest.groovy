package com.mqgateway.core.device.motiondetector

import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.mqgateway.core.io.provider.DefaultMySensorsInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.mysensors.MySensorsSerialConnection
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class MotionSensorDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new MotionSensorDeviceFactory(testGatewayFactory.ioProvider)

  def "should create motion sensor"() {
    given:
    def deviceConfig = new DeviceConfiguration("myMotionDetector", "Test MotionDetector", DeviceType.MOTION_DETECTOR, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig, [] as Set)

    then:
    device.id == "myMotionDetector"
    device.type == DeviceType.MOTION_DETECTOR
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfig = new DeviceConfiguration("myMotionDetector", "Test MotionDetector", DeviceType.MOTION_DETECTOR)

    when:
    factory.create(deviceConfig, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }


}
