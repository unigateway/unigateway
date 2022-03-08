package com.mqgateway.core.device.gate

import static com.mqgateway.utils.TestGatewayFactory.gateway

import com.mqgateway.core.device.relay.RelayDeviceFactory
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.InternalDeviceConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

@Ignore("Gate factory not implemented yet")
class GateDeviceFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(), new MySensorsInputOutputProvider())

  @Subject
  def factory = new RelayDeviceFactory(ioProvider)

  def "should create three buttons gate device when gate device configuration has three buttons configured"() {
    given:
    List<DeviceConfiguration> devicesConfiguration = [
      new DeviceConfiguration("stopButton", "es1", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("openButton", "es2", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(2)]),
      new DeviceConfiguration("closeButton", "es3", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(3)]),
      new DeviceConfiguration("closedReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(4)]),
      new DeviceConfiguration("myGate", "Test gate device", DeviceType.GATE, [:],
                              [
                                stopButton      : new InternalDeviceConfiguration("stopButton"),
                                openButton      : new InternalDeviceConfiguration("openButton"),
                                closeButton     : new InternalDeviceConfiguration("closeButton"),
                                closedReedSwitch: new InternalDeviceConfiguration("closedReedSwitch")
                              ])
    ]
    GatewayConfiguration gateway = gateway(devicesConfiguration)

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.last()
    device instanceof ThreeButtonsGateDevice
    device.id == "myGate"
    device.type == DeviceType.GATE
  }

  def "should create single button gate device when gate device configuration has action button configured only"() {
    given:
    def deviceConfig = new DeviceConfiguration("myGateSingleButton", "Test gate device", DeviceType.GATE, [:],
                                               [
                                                 actionButton    : new InternalDeviceConfiguration("actionButton"),
                                                 openReedSwitch  : new InternalDeviceConfiguration("openReedSwitch"),
                                                 closedReedSwitch: new InternalDeviceConfiguration("closedReedSwitch")
                                               ])
    when:
    def device = factory.create(deviceConfig)

    then:
    device instanceof SingleButtonsGateDevice
    device.id == "myGateSingleButton"
    device.type == DeviceType.GATE
  }

}
