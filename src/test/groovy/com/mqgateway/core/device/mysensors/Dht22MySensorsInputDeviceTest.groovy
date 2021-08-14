package com.mqgateway.core.device.mysensors

import static com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.hardware.simulated.SimulatedSerial
import com.mqgateway.mysensors.MySensorMessageParser
import com.mqgateway.mysensors.MySensorsSerialConnection
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class Dht22MySensorsInputDeviceTest extends Specification {
  UpdateListenerStub listenerStub = new UpdateListenerStub()
  SimulatedSerial simulatedSerial = new SimulatedSerial()
  MySensorsSerialConnection mySensorsSerialConnection = new MySensorsSerialConnection(simulatedSerial, new MySensorMessageParser())

  @Subject
  Dht22MySensorsInputDevice device = new Dht22MySensorsInputDevice("dht-device", 3, mySensorsSerialConnection)

  void setup() {
    mySensorsSerialConnection.init()
  }

  def "should notify when receiving information about #sensor change"(String sensor, String message, String newValueExpected) {
    given:
    device.addListener(listenerStub)
    device.init()

    when:
    simulatedSerial.sendFakeMessage(message)

    then:
    listenerStub.receivedUpdates.contains(new UpdateListenerStub.Update("dht-device", sensor, newValueExpected))

    where:
    sensor                 | message             | newValueExpected
    TEMPERATURE.toString() | "3;1;1;0;0;36.5\n"  | "36.5"
    HUMIDITY.toString()    | "3;0;1;1;0;66.32\n" | "66.32"
  }

  def "should notify about state change to OFFLINE when error in debug message has been received"() {
    given:
    device.addListener(listenerStub)
    device.init()
    String message = "3;2;3;0;28;ERROR: DHT22 init failed\n"

    when:
    simulatedSerial.sendFakeMessage(message)

    then:
    listenerStub.receivedUpdates.contains(new UpdateListenerStub.Update("dht-device", STATE.toString(), "OFFLINE"))
  }

  def "should notify about state change back to ONLINE when any other message is received after going OFFLINE"() {
    given:
    device.addListener(listenerStub)
    device.init()
    String errorMessage = "3;2;3;0;28;ERROR: DHT22 init failed\n"
    String temperatureMessage = "3;1;1;0;0;36.5\n"

    when:
    simulatedSerial.sendFakeMessage(errorMessage)
    simulatedSerial.sendFakeMessage(temperatureMessage)


    then:
    def updatesWithoutLastPing = listenerStub.receivedUpdates -
      listenerStub.receivedUpdates.findAll { it.propertyId == DevicePropertyType.LAST_PING.toString() }

    updatesWithoutLastPing == [
      new UpdateListenerStub.Update("dht-device", STATE.toString(), "OFFLINE"),
      new UpdateListenerStub.Update("dht-device", TEMPERATURE.toString(), "36.5"),
      new UpdateListenerStub.Update("dht-device", STATE.toString(), "ONLINE")
    ]
  }
}
