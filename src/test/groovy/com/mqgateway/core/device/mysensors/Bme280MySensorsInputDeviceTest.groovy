package com.mqgateway.core.device.mysensors

import static com.mqgateway.core.gatewayconfig.DevicePropertyType.AVAILABILITY
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.PRESSURE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.hardware.simulated.SimulatedSerial
import com.mqgateway.mysensors.MySensorMessageParser
import com.mqgateway.mysensors.MySensorsSerialConnection
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class Bme280MySensorsInputDeviceTest extends Specification {

  UpdateListenerStub listenerStub = new UpdateListenerStub()
  SimulatedSerial simulatedSerial = new SimulatedSerial()
  MySensorsSerialConnection mySensorsSerialConnection = new MySensorsSerialConnection(simulatedSerial, new MySensorMessageParser())

  @Subject
  Bme280MySensorsInputDevice device = new Bme280MySensorsInputDevice("bme-device", 3, mySensorsSerialConnection)

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
    listenerStub.receivedUpdates.contains(new UpdateListenerStub.Update("bme-device", sensor, newValueExpected))

    where:
    sensor                 | message             | newValueExpected
    TEMPERATURE.toString() | "3;1;1;0;0;36.5\n"  | "36.5"
    HUMIDITY.toString()    | "3;0;1;1;0;66.32\n" | "66.32"
    PRESSURE.toString()    | "3;2;1;4;0;99845\n" | "99845"
  }

  def "should notify about state change to OFFLINE when error in debug message has been received"() {
    given:
    device.addListener(listenerStub)
    device.init()
    String message = "3;32;3;0;28;ERROR: BME280 init failed\n"

    when:
    simulatedSerial.sendFakeMessage(message)

    then:
    listenerStub.receivedUpdates.contains(new UpdateListenerStub.Update("bme-device", AVAILABILITY.toString(), "OFFLINE"))
  }

  def "should notify about state change back to ONLINE when any other message is received after going OFFLINE"() {
    given:
    device.addListener(listenerStub)
    device.init()
    String errorMessage = "3;32;3;0;28;ERROR: BME280 init failed\n"
    String temperatureMessage = "3;1;1;0;0;36.5\n"

    when:
    simulatedSerial.sendFakeMessage(errorMessage)
    simulatedSerial.sendFakeMessage(temperatureMessage)


    then:
    def updatesWithoutLastPing = listenerStub.receivedUpdates -
      listenerStub.receivedUpdates.findAll { it.propertyId == DevicePropertyType.LAST_PING.toString() }

    updatesWithoutLastPing == [
      new UpdateListenerStub.Update("bme-device", AVAILABILITY.toString(), "OFFLINE"),
      new UpdateListenerStub.Update("bme-device", TEMPERATURE.toString(), "36.5"),
      new UpdateListenerStub.Update("bme-device", AVAILABILITY.toString(), "ONLINE")
    ]
  }
}
