package com.mqgateway.core.device.mysensors

import static com.mqgateway.core.gatewayconfig.DevicePropertyType.AVAILABILITY
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.hardware.simulated.SimulatedSerial
import com.mqgateway.mysensors.MySensorMessageParser
import com.mqgateway.mysensors.MySensorsSerialConnection
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class MotionSensorMySensorsInputDeviceTest extends Specification {
  UpdateListenerStub listenerStub = new UpdateListenerStub()
  SimulatedSerial simulatedSerial = new SimulatedSerial()
  MySensorsSerialConnection mySensorsSerialConnection = new MySensorsSerialConnection(simulatedSerial, new MySensorMessageParser())

  @Subject
  MotionSensorMySensorsInputDevice device = new MotionSensorMySensorsInputDevice("motion-device", 3, mySensorsSerialConnection)

  void setup() {
    mySensorsSerialConnection.init()
  }

  def "should notify when receiving information about motion detected"() {
    given:
    device.addListener(listenerStub)
    device.init()

    when:
    simulatedSerial.sendFakeMessage("3;3;1;0;0;1\n")

    then:
    listenerStub.receivedUpdates.contains(new UpdateListenerStub.Update("motion-device", STATE.toString(), "ON"))
  }

  def "should notify when receiving information about motion stopped"() {
    given:
    device.addListener(listenerStub)
    device.init()

    when:
    simulatedSerial.sendFakeMessage("3;3;1;0;0;1\n")
    simulatedSerial.sendFakeMessage("3;3;1;0;0;0\n")

    then:
    listenerStub.receivedUpdates.containsAll([
      new UpdateListenerStub.Update("motion-device", STATE.toString(), "ON"),
      new UpdateListenerStub.Update("motion-device", STATE.toString(), "OFF")
    ])
  }

  def "should notify about state change to OFFLINE when error in debug message has been received"() {
    given:
    device.addListener(listenerStub)
    device.init()
    String message = "3;32;3;0;28;ERROR: Some error\n"

    when:
    simulatedSerial.sendFakeMessage(message)

    then:
    listenerStub.receivedUpdates.contains(new UpdateListenerStub.Update("motion-device", AVAILABILITY.toString(), "OFFLINE"))
  }

  def "should notify about state change back to ONLINE when any other message is received after going OFFLINE"() {
    given:
    device.addListener(listenerStub)
    device.init()
    String errorMessage = "3;32;3;0;28;ERROR: Some error\n"
    String motionMessage = "3;3;1;0;0;1\n"

    when:
    simulatedSerial.sendFakeMessage(errorMessage)
    simulatedSerial.sendFakeMessage(motionMessage)


    then:
    def updatesWithoutLastPing = listenerStub.receivedUpdates -
      listenerStub.receivedUpdates.findAll { it.propertyId == DevicePropertyType.LAST_PING.toString() }

    updatesWithoutLastPing == [
      new UpdateListenerStub.Update("motion-device", AVAILABILITY.toString(), "OFFLINE"),
      new UpdateListenerStub.Update("motion-device", STATE.toString(), "ON"),
      new UpdateListenerStub.Update("motion-device", AVAILABILITY.toString(), "ONLINE")
    ]
  }
}
