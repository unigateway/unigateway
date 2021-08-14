package com.mqgateway.core.device

import static com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.PRESSURE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import com.mqgateway.core.device.mysensors.Bme280MySensorsInputDevice
import com.mqgateway.core.device.mysensors.Dht22MySensorsInputDevice
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.core.hardware.simulated.SimulatedExpanderPinProvider
import com.mqgateway.core.hardware.simulated.SimulatedGpioController
import com.mqgateway.core.hardware.simulated.SimulatedMcpExpanders
import com.mqgateway.core.hardware.simulated.SimulatedSerial
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.mysensors.MySensorMessageParser
import com.mqgateway.mysensors.MySensorsSerialConnection
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import spock.lang.Specification
import spock.lang.Subject

class DeviceFactoryTest extends Specification {

	SimulatedExpanderPinProvider pinProvider = new SimulatedExpanderPinProvider(new SimulatedGpioController(), new SimulatedMcpExpanders([]))

  SimulatedSerial simulatedSerial = new SimulatedSerial()
  MySensorsSerialConnection mySensorsSerialConnection = new MySensorsSerialConnection(simulatedSerial, new MySensorMessageParser())

  @Subject
  DeviceFactory deviceFactory = new DeviceFactory(pinProvider, new TimersScheduler(), mySensorsSerialConnection, new FakeSystemInfoProvider())

  def "should create relay"() {
		given:
		def relayDeviceConfig = new DeviceConfig("myRelay", "Test relay", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [relayDeviceConfig])])])

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
		def deviceConfig = new DeviceConfig("mySwitchButton", "Test switchButton", DeviceType.SWITCH_BUTTON, [WireColor.GREEN],
											["debounceMs": "124"], [:])
		Gateway gateway = gateway([room([point("point name", 5, [deviceConfig])])])
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
		def deviceConfig = new DeviceConfig("myReedSwitch", "Test ReedSwitch", DeviceType.REED_SWITCH, [WireColor.GREEN_WHITE], ["debounceMs": "54"], [:])
		Gateway gateway = gateway([room([point("point name", 4, [deviceConfig])])])
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
		def deviceConfig = new DeviceConfig("myMotionDetector", "Test MotionDetector", DeviceType.MOTION_DETECTOR, [WireColor.BLUE], ["debounceMs": "1"], [:])
		Gateway gateway = gateway([room([point("point name", 12, [deviceConfig])])])
		pinProvider.pinDigitalInput(12, WireColor.BLUE, "myMotionDetector_pin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof MotionSensorDevice
		device.id == "myMotionDetector"
		device.type == DeviceType.MOTION_DETECTOR
	}

	def "should create BME280"() {
		given:
		def deviceConfig = new DeviceConfig("myBME280", "Test BME280 device", DeviceType.BME280, [WireColor.BROWN, WireColor.BROWN_WHITE],
                                        [mySensorsNodeId: "11"], [:])
		Gateway gateway = gateway([room([point("point name", 10, [deviceConfig])])])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof Bme280MySensorsInputDevice
		device.id == "myBME280"
		device.type == DeviceType.BME280
	}

  def "should create BME280 with custom childSensorIds"() {
    given:
    def listenerStub = new UpdateListenerStub()
    def deviceConfig = new DeviceConfig("myBME280", "Test BME280 device", DeviceType.BME280, [WireColor.BROWN, WireColor.BROWN_WHITE],
                                        [
                                          mySensorsNodeId: "11",
                                          humidityChildSensorId: '7',
                                          temperatureChildSensorId: '8',
                                          pressureChildSensorId: '9',
                                          debugChildSensorId: '10'
                                        ])
    Gateway gateway = gateway([room([point("point name", 10, [deviceConfig])])])
    mySensorsSerialConnection.init()

    when:
    def devices = deviceFactory.createAll(gateway)
    def bme280Device = devices.last()
    bme280Device.addListener(listenerStub)
    bme280Device.init(true)
    simulatedSerial.sendFakeMessage("11;7;1;0;1;65.4\n")
    simulatedSerial.sendFakeMessage("11;8;1;0;0;36.63\n")
    simulatedSerial.sendFakeMessage("11;9;1;0;4;123456\n")
    simulatedSerial.sendFakeMessage("11;10;3;0;28;debug message\n")

    then:
    listenerStub.receivedUpdates.containsAll([
      new UpdateListenerStub.Update("myBME280", HUMIDITY.toString(), "65.4"),
      new UpdateListenerStub.Update("myBME280", TEMPERATURE.toString(), "36.63"),
      new UpdateListenerStub.Update("myBME280", PRESSURE.toString(), "123456")
    ])
  }

  def "should create DHT22"() {
    given:
    def deviceConfig = new DeviceConfig("myDHT22", "Test DHT22 device", DeviceType.DHT22, [WireColor.BROWN, WireColor.BROWN_WHITE],
                                        [mySensorsNodeId: "2"], [:])
    Gateway gateway = gateway([room([point("point name", 10, [deviceConfig])])])

    when:
    def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.last()
		device instanceof Dht22MySensorsInputDevice
		device.id == "myDHT22"
		device.type == DeviceType.DHT22
	}

  def "should create DTH22 with custom childSensorIds"() {
    given:
    def listenerStub = new UpdateListenerStub()
    def deviceConfig = new DeviceConfig("myDHT22", "Test DHT22 device", DeviceType.DHT22, [WireColor.BROWN, WireColor.BROWN_WHITE],
                                        [
                                          mySensorsNodeId: "2",
                                          humidityChildSensorId: '12',
                                          temperatureChildSensorId: '13',
                                          debugChildSensorId: '99'
                                        ])
    Gateway gateway = gateway([room([point("point name", 7, [deviceConfig])])])
    mySensorsSerialConnection.init()

    when:
    def devices = deviceFactory.createAll(gateway)
    def dht22Device = devices.last()
    dht22Device.addListener(listenerStub)
    dht22Device.init(true)
    simulatedSerial.sendFakeMessage("2;12;1;0;1;45.4\n")
    simulatedSerial.sendFakeMessage("2;13;1;0;0;19.41\n")
    simulatedSerial.sendFakeMessage("2;99;3;0;28;debug message\n")

    then:
    listenerStub.receivedUpdates.containsAll([
      new UpdateListenerStub.Update("myDHT22", HUMIDITY.toString(), "45.4"),
      new UpdateListenerStub.Update("myDHT22", TEMPERATURE.toString(), "19.41"),
    ])
  }

	def "should create timer switch"() {
		given:
		def timerSwitchDeviceConfig = new DeviceConfig("myTimerSwitch", "Test timer switch", DeviceType.TIMER_SWITCH, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [timerSwitchDeviceConfig])])])
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
		def deviceConfig = new DeviceConfig("myShutter", "Test shutter device", DeviceType.SHUTTER, [],
											[fullOpenTimeMs: "1000", fullCloseTimeMs: "800"],
											[
												stopRelay: new DeviceConfig("stopRelay", "relay1", DeviceType.RELAY, [WireColor.BLUE], [:], [:]),
												upDownRelay: new DeviceConfig("upDownRelay", "relay2", DeviceType.RELAY, [WireColor.GREEN], [:], [:])
											])
		Gateway gateway = gateway([room([point("point name", 14, [deviceConfig])])])

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
    def deviceConfig = new DeviceConfig("myGate", "Test gate device", DeviceType.GATE, [], [:],
                      [
                        stopButton      : new DeviceConfig("stopButton", "es1", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE], [:], [:]),
                        openButton      : new DeviceConfig("openButton", "es2", DeviceType.EMULATED_SWITCH, [WireColor.BLUE], [:], [:]),
                        closeButton     : new DeviceConfig("closeButton", "es3", DeviceType.EMULATED_SWITCH, [WireColor.GREEN_WHITE], [:], [:]),
                        closedReedSwitch: new DeviceConfig("closedReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:])
                      ])
		Gateway gateway = gateway([room([point("point name", 15, [deviceConfig])])])

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
    def deviceConfig = new DeviceConfig("myGateSingleButton", "Test gate device", DeviceType.GATE, [], [:],
                    [
                      actionButton    : new DeviceConfig("actionButton", "es1", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE], [:], [:]),
                      openReedSwitch  : new DeviceConfig("openReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:]),
                      closedReedSwitch: new DeviceConfig("closedReedSwitch", "reedSwitch2", DeviceType.REED_SWITCH, [WireColor.GREEN_WHITE], [:], [:])
                    ])
		Gateway gateway = gateway([room([point("point name", 15, [deviceConfig])])])

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
    def reedSwitchConfig = new DeviceConfig("closedReedSwitch", "Referenced reed switch", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:])
    def deviceConfig = new DeviceConfig("gateWithReferencedReedSwitch", "Test gate device", DeviceType.GATE, [], [:],
                    [
                      actionButton    : new DeviceConfig("actionButton", "es1", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE], [:], [:]),
                      closedReedSwitch: new DeviceConfig("closedReedSwitchReference", "reedSwitchReference", DeviceType.REFERENCE, [], [:], [:], "closedReedSwitch")
                    ])
		Gateway gateway = gateway([room([point("point name", 15, [deviceConfig]), point("another point", 12, [reedSwitchConfig])])])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.find {it instanceof SingleButtonsGateDevice}
		device.id == "gateWithReferencedReedSwitch"
		device.type == DeviceType.GATE
	}

    def "should create each device only once when it is referenced"() {
    def reedSwitchConfig = new DeviceConfig("closedReedSwitch", "Referenced reed switch", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:])
    def referenceDeviceConfig = new DeviceConfig("referenceDevice", "Reference device", DeviceType.REFERENCE, [], [:], [:], "closedReedSwitch")
    Gateway gateway = gateway([room([point("point name", 15, [referenceDeviceConfig]), point("another point", 12, [reedSwitchConfig])])])

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
		Gateway gateway = gateway([])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof MqGatewayDevice
		device.id == "gtwName"
		device.type == DeviceType.MQGATEWAY
	}
}
