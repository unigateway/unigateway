package com.mqgateway.core.device.light


import com.mqgateway.core.device.relay.RelayDevice
import com.mqgateway.core.device.switchbutton.SwitchButtonDevice
import com.mqgateway.core.hardware.simulated.SimulatedBinaryInput
import com.mqgateway.core.hardware.simulated.SimulatedBinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class LightDeviceTest extends Specification {

  SimulatedBinaryOutput binaryOutput = new SimulatedBinaryOutput()
  RelayDevice relay = new RelayDevice("relay1", "Relay device", binaryOutput, BinaryState.LOW, [:])
  TestWrappingSwitchButtonDevice switch1 = TestWrappingSwitchButtonDevice.create("switch1", "Switch Button 1")
  TestWrappingSwitchButtonDevice switch2 = TestWrappingSwitchButtonDevice.create("switch2", "Switch Button 2")

  @Subject
  LightDevice light = new LightDevice("light1", "Light 1", relay, [switch1.device, switch2.device], [:])

  def "should change relay state to #expectedRelayState when light state changed to #newLightState"(String newLightState, String expectedRelayState) {
    given:
    def relayListenerStub = new UpdateListenerStub()
    relay.addListener(relayListenerStub)
    relay.init()
    light.init()

    when:
    light.change("state", newLightState)

    then:
    relayListenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("relay1", "state", expectedRelayState)

    where:
    newLightState || expectedRelayState
    "ON"          || "ON"
    "OFF"         || "OFF"
  }

  def "should change light state to #newLightState when current light state is #currentLightState and any switch is pressed"(String currentLightState, String newLightState, int switchIndex) {
    given:
    List<TestWrappingSwitchButtonDevice> switches = [switch1, switch2]
    switches.forEach { it.device.init(false) }
    def lightListenerStub = new UpdateListenerStub()
    relay.init(false)
    light.initProperty("state", currentLightState)
    light.addListener(lightListenerStub)
    light.init(true)

    when:
    switches[switchIndex].press()

    then:
    lightListenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("light1", "state", newLightState)

    where:
    currentLightState | newLightState | switchIndex
    "ON"              | "OFF"         | 0
    "OFF"             | "ON"          | 0
    "ON"              | "OFF"         | 1
    "OFF"             | "ON"          | 1
  }

  def "should initialize light with ON when it has been passed on property initialization"() {
    given:
    def lightListenerStub = new UpdateListenerStub()
    relay.init(false)
    light.addListener(lightListenerStub)

    when:
    light.initProperty("state", "ON")

    then:
    lightListenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("light1", "state", "ON")
  }

  def "should throw exception when trying to change to unknown status"() {
    given:
    light.init()

    when:
    light.change("state", "INCORRECT_STATE")

    then:
    thrown(LightDevice.UnknownLightStateException)
  }

  static final class TestWrappingSwitchButtonDevice {

    final SwitchButtonDevice device
    private final SimulatedBinaryInput binaryInput

    private TestWrappingSwitchButtonDevice(SwitchButtonDevice device, SimulatedBinaryInput binaryInput) {
      this.device = device
      this.binaryInput = binaryInput
    }

    static TestWrappingSwitchButtonDevice create(String id, String name) {
      SimulatedBinaryInput binaryInput = new SimulatedBinaryInput(BinaryState.HIGH)
      SwitchButtonDevice device = new SwitchButtonDevice(id, name, binaryInput, 100, [:])
      return new TestWrappingSwitchButtonDevice(device, binaryInput)
    }

    void press() {
      binaryInput.low()
    }

    void release() {
      binaryInput.high()
    }
  }
}
