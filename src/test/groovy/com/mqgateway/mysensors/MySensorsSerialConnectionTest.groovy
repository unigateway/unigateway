package com.mqgateway.mysensors

import com.mqgateway.core.hardware.simulated.SimulatedSerial
import org.jetbrains.annotations.NotNull
import spock.lang.Specification
import spock.lang.Subject

class MySensorsSerialConnectionTest extends Specification {

  private SimulatedSerial simulatedSerial = new SimulatedSerial()

  @Subject
  MySensorsSerialConnection mySensorsSerialConnection = new MySensorsSerialConnection(simulatedSerial, new MySensorMessageParser())

  void setup() {
    mySensorsSerialConnection.init()
  }

  def "should notify listener when message received for its nodeId"() {
    given:
    int nodeId = 241
    def listener = new MySensorsSimpleListener()
    mySensorsSerialConnection.registerDeviceListener(nodeId, listener)

    when:
    simulatedSerial.sendFakeMessage("241;5;2;1;37;42\n")

    then:
    listener.receivedMessages[0] == new Message(nodeId, 5, Command.REQ, true, SetReqType.V_LEVEL, "42")
  }

  def "should notify listeners all involved listeners when multiple messages received"() {
    given:
    List<Integer> nodesIds = [1, 2, 3, 4]
    Map<Integer, MySensorsSimpleListener> nodesListeners = nodesIds.collectEntries { nodeId -> [(nodeId): new MySensorsSimpleListener()] }
    nodesListeners.each { mySensorsSerialConnection.registerDeviceListener(it.key, it.value) }

    when:
    simulatedSerial.sendFakeMessage("1;5;2;1;37;11\n1;5;2;1;37;12\n3;5;2;1;37;31\n4;5;2;1;37;41\n")

    then:
    nodesListeners[1].receivedMessages[0] == new Message(1, 5, Command.REQ, true, SetReqType.V_LEVEL, "11")
    nodesListeners[1].receivedMessages[1] == new Message(1, 5, Command.REQ, true, SetReqType.V_LEVEL, "12")
    nodesListeners[3].receivedMessages[0] == new Message(3, 5, Command.REQ, true, SetReqType.V_LEVEL, "31")
    nodesListeners[4].receivedMessages[0] == new Message(4, 5, Command.REQ, true, SetReqType.V_LEVEL, "41")
  }

  def "should not receive any message when nodeId is different than listener waiting for message"() {
    given:
    int nodeId = 241
    def listener = new MySensorsSimpleListener()
    mySensorsSerialConnection.registerDeviceListener(nodeId, listener)

    when:
    simulatedSerial.sendFakeMessage("100;5;2;1;37;42\n")

    then:
    listener.receivedMessages.isEmpty()
  }

  def "should send serialized message to serial when publishing message"() {
    given:
    Message message = new Message(111, 0, Command.SET, true, SetReqType.V_HUM, "65.3")

    when:
    mySensorsSerialConnection.publishMessage(message)

    then:
    simulatedSerial.messagesWrittenToSerial == ["111;0;1;1;1;65.3\n"]
  }
}


class MySensorsSimpleListener implements MySensorsSerialListener {

  List<Message> receivedMessages = []

  @Override
  void onMessageReceived(@NotNull Message message) {
    receivedMessages.add(message)
  }
}