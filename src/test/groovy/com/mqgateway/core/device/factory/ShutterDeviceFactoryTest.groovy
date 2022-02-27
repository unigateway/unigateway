package com.mqgateway.core.device.factory


import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.InternalDeviceConfiguration
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

@Ignore("Shutter factory not implemented yet")
class ShutterDeviceFactoryTest extends Specification {

  @Subject
  def factory = new ShutterDeviceFactory()

  def "should create shutter"() {
    given:
    def deviceConfig = new DeviceConfiguration("myShutter", "Test shutter device", DeviceType.SHUTTER, [:], [
      stopRelay  : new InternalDeviceConfiguration("stopRelay"),
      upDownRelay: new InternalDeviceConfiguration("upDownRelay")
    ], [fullOpenTimeMs: "1000", fullCloseTimeMs: "800"])

    when:
    def device = factory.create(deviceConfig)

    then:
    device.id == "myShutter"
    device.type == DeviceType.SHUTTER
  }

}
