package com.mqgateway.core.device.factory

import static com.mqgateway.utils.TestGatewayFactory.gateway

import com.mqgateway.core.device.MotionSensorDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import spock.lang.Specification
import spock.lang.Subject

class MotionSensorDeviceFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(), new MySensorsInputOutputProvider())

  @Subject
  def factory = new MotionSensorDeviceFactory(ioProvider)

  def "should create motion sensor"() {
    given:
    def deviceConfig = new DeviceConfiguration("myMotionDetector", "Test MotionDetector", DeviceType.MOTION_DETECTOR, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig)

    then:
    device.id == "myMotionDetector"
    device.type == DeviceType.MOTION_DETECTOR
  }

}
