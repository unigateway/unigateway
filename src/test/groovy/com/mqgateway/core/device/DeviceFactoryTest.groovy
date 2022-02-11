package com.mqgateway.core.device

import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.core.hardware.simulated.SimulatedExpanderPinProvider
import com.mqgateway.core.hardware.simulated.SimulatedGpioController
import com.mqgateway.core.hardware.simulated.SimulatedMcpExpanders
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import spock.lang.Specification
import spock.lang.Subject

class DeviceFactoryTest extends Specification {

	SimulatedExpanderPinProvider pinProvider = new SimulatedExpanderPinProvider(new SimulatedGpioController(), new SimulatedMcpExpanders([]))

  @Subject
  DeviceFactory deviceFactory = new DeviceFactory(pinProvider, new TimersScheduler(), new FakeSystemInfoProvider())

  def "should create relay"() {
		given:
		def relayDeviceConfig = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
		GatewayConfiguration gateway = gateway([room([point("point name", 2, [relayDeviceConfig])])])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof RelayDevice
		device.id == "myRelay"
		device.type == DeviceType.RELAY
	}

	def "should create switch button"() {
		given:
		def deviceConfig = new DeviceConfiguration("mySwitchButton", "Test switchButton", DeviceType.SWITCH_BUTTON, [WireColor.GREEN],
												   ["debounceMs": "124"], [:])
		GatewayConfiguration gateway = gateway([room([point("point name", 5, [deviceConfig])])])
		pinProvider.pinDigitalInput(5, WireColor.GREEN, "mySwitchButton_pin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof SwitchButtonDevice
		device.id == "mySwitchButton"
		device.type == DeviceType.SWITCH_BUTTON
	}

	def "should create reed switch"() {
		given:
		def deviceConfig = new DeviceConfiguration("myReedSwitch", "Test ReedSwitch", DeviceType.REED_SWITCH, [WireColor.GREEN_WHITE], ["debounceMs": "54"], [:])
		GatewayConfiguration gateway = gateway([room([point("point name", 4, [deviceConfig])])])
		pinProvider.pinDigitalInput(4, WireColor.GREEN_WHITE, "myReedSwitch_pin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof ReedSwitchDevice
		device.id == "myReedSwitch"
		device.type == DeviceType.REED_SWITCH
	}

	def "should create motion detector"() {
		given:
		def deviceConfig = new DeviceConfiguration("myMotionDetector", "Test MotionDetector", DeviceType.MOTION_DETECTOR, [WireColor.BLUE], ["debounceMs": "1"], [:])
		GatewayConfiguration gateway = gateway([room([point("point name", 12, [deviceConfig])])])
		pinProvider.pinDigitalInput(12, WireColor.BLUE, "myMotionDetector_pin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof MotionSensorDevice
		device.id == "myMotionDetector"
		device.type == DeviceType.MOTION_DETECTOR
	}

	def "should create timer switch"() {
		given:
		def timerSwitchDeviceConfig = new DeviceConfiguration("myTimerSwitch", "Test timer switch", DeviceType.TIMER_SWITCH, [WireColor.BLUE], [:], [:])
		GatewayConfiguration gateway = gateway([room([point("point name", 2, [timerSwitchDeviceConfig])])])
		pinProvider.pinDigitalOutput(2, WireColor.BLUE, "myTimerSwitch_pin", PinState.HIGH) >> Mock(GpioPinDigitalOutput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof TimerSwitchRelayDevice
		device.id == "myTimerSwitch"
		device.type == DeviceType.TIMER_SWITCH
	}

	def "should create shutter"() {
		given:
		def deviceConfig = new DeviceConfiguration("myShutter", "Test shutter device", DeviceType.SHUTTER, [],
												   [fullOpenTimeMs: "1000", fullCloseTimeMs: "800"],
												   [
												stopRelay: new DeviceConfiguration("stopRelay", "relay1", DeviceType.RELAY, [WireColor.BLUE], [:], [:]),
												upDownRelay: new DeviceConfiguration("upDownRelay", "relay2", DeviceType.RELAY, [WireColor.GREEN], [:], [:])
											])
		GatewayConfiguration gateway = gateway([room([point("point name", 14, [deviceConfig])])])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof ShutterDevice
		device.id == "myShutter"
		device.type == DeviceType.SHUTTER
	}

	def "should create three buttons gate device when gate device configuration has three buttons configured"() {
		given:
    def deviceConfig = new DeviceConfiguration("myGate", "Test gate device", DeviceType.GATE, [], [:],
											   [
                        stopButton      : new DeviceConfiguration("stopButton", "es1", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE], [:], [:]),
                        openButton      : new DeviceConfiguration("openButton", "es2", DeviceType.EMULATED_SWITCH, [WireColor.BLUE], [:], [:]),
                        closeButton     : new DeviceConfiguration("closeButton", "es3", DeviceType.EMULATED_SWITCH, [WireColor.GREEN_WHITE], [:], [:]),
                        closedReedSwitch: new DeviceConfiguration("closedReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:])
                      ])
		GatewayConfiguration gateway = gateway([room([point("point name", 15, [deviceConfig])])])

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
    def deviceConfig = new DeviceConfiguration("myGateSingleButton", "Test gate device", DeviceType.GATE, [], [:],
											   [
                      actionButton    : new DeviceConfiguration("actionButton", "es1", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE], [:], [:]),
                      openReedSwitch  : new DeviceConfiguration("openReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:]),
                      closedReedSwitch: new DeviceConfiguration("closedReedSwitch", "reedSwitch2", DeviceType.REED_SWITCH, [WireColor.GREEN_WHITE], [:], [:])
                    ])
		GatewayConfiguration gateway = gateway([room([point("point name", 15, [deviceConfig])])])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof SingleButtonsGateDevice
		device.id == "myGateSingleButton"
		device.type == DeviceType.GATE
	}

	def "should create device with internal devices referencing to other devices"() {
		given:
    def reedSwitchConfig = new DeviceConfiguration("closedReedSwitch", "Referenced reed switch", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:])
    def deviceConfig = new DeviceConfiguration("gateWithReferencedReedSwitch", "Test gate device", DeviceType.GATE, [], [:],
											   [
                      actionButton    : new DeviceConfiguration("actionButton", "es1", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE], [:], [:]),
                      closedReedSwitch: new DeviceConfiguration("closedReedSwitchReference", "reedSwitchReference", DeviceType.REFERENCE, [], [:], [:], "closedReedSwitch")
                    ])
		GatewayConfiguration gateway = gateway([room([point("point name", 15, [deviceConfig]), point("another point", 12, [reedSwitchConfig])])])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.find {it instanceof SingleButtonsGateDevice}
		device.id == "gateWithReferencedReedSwitch"
		device.type == DeviceType.GATE
	}

    def "should create each device only once when it is referenced"() {
    def reedSwitchConfig = new DeviceConfiguration("closedReedSwitch", "Referenced reed switch", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:])
    def referenceDeviceConfig = new DeviceConfiguration("referenceDevice", "Reference device", DeviceType.REFERENCE, [], [:], [:], "closedReedSwitch")
    GatewayConfiguration gateway = gateway([room([point("point name", 15, [referenceDeviceConfig]), point("another point", 12, [reedSwitchConfig])])])

    when:
    def createdDevices = deviceFactory.createAll(gateway)

    then:
    def devices = createdDevices.findAll {it.type != DeviceType.MQGATEWAY }
    devices.size() == 1
    devices[0].type == DeviceType.REED_SWITCH
    devices[0].id == "closedReedSwitch"
  }

	def "should create MqGateway as a device"() {
		given:
		GatewayConfiguration gateway = gateway([])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof MqGatewayDevice
		device.id == "gtwName"
		device.type == DeviceType.MQGATEWAY
	}
}
