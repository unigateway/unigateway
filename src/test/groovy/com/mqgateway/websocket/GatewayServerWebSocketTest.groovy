package com.mqgateway.websocket

import static groovy.json.JsonOutput.toJson

import com.mqgateway.ApplicationKt
import com.mqgateway.MqGateway
import groovy.json.JsonSlurper
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.websocket.RxWebSocketClient
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.ClientWebSocket
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest(application = ApplicationKt.class, packages = ["com.mqgateway"])
class GatewayServerWebSocketTest extends Specification {

  @Inject
  EmbeddedServer embeddedServer

  @Inject
  @Client("/")
  RxWebSocketClient webSocketClient

  @Shared
  @Inject
  MqGateway mqGateway

  def conditions = new PollingConditions(timeout: 10)

  TestWebSocketClient client

  void setupSpec() {
    mqGateway.initialize()
  }

  void setup() {
    client = webSocketClient
      .connect(TestWebSocketClient.class, "/devices/tests")
      .blockingFirst()
  }

  void cleanup() {
    client.close()
  }

  def "should receive current devices states when connecting to websocket"() {
    expect:
    conditions.within(5) {
      def initialStateMessage = client.getReceivedMessages().find { it.type == "INITIAL_STATE_LIST" }
      assert (initialStateMessage.message as List).collect { it.deviceId }
        .containsAll(["workshop_light_switch", "bedroom_light_switch", "bedroom_shutter", "right_garage_door"])
      def deviceState = initialStateMessage.message.find { it.deviceId == "workshop_light_switch" }
      assert deviceState["properties"].find{ Object property -> property.propertyId == "state" }.value == "RELEASED"
    }
  }

  def "should change device property value when receiving DeviceChangeMessage"() {
    when:
    client.send(toJson([deviceId: "workshop_light", propertyId: "state", newValue: "ON"]))

    then:
    conditions.within(5) {
      def updateMessage = client.getReceivedMessages().find { it.type == "STATE_UPDATE" && it.message.deviceId == "workshop_light" }.message
      assert updateMessage.newValue == "ON"
    }
  }

  def "should send OK acknowledge to client when received DeviceChangeMessage to existing device"() {
    when:
    client.send(toJson([deviceId: "workshop_light", propertyId: "state", newValue: "ON"]))

    then:
    conditions.within(5) {
      def ackMessage = client.getReceivedMessages().find { it.type == "ACKNOWLEDGE" }.message
      assert ackMessage == "OK"
    }
  }

  def "should send DeviceNotFoundError reject to client when received DeviceChangeMessage to non-existing device"() {
    when:
    client.send(toJson([deviceId: "non_existing_device", propertyId: "state", newValue: "ON"]))

    then:
    conditions.within(5) {
      def ackMessage = client.getReceivedMessages().find { it.type == "REJECT" }.message
      assert ackMessage == "Device with id 'non_existing_device' not found"
    }
  }

  def "should send error to client when received DeviceChangeMessage to input device"() {
    when:
    client.send(toJson([deviceId: "workshop_light_switch", propertyId: "state", newValue: "ON"]))

    then:
    conditions.within(5) {
      assert client.getReceivedMessages().find { it.type == "ERROR" }
    }
  }
}


@ClientWebSocket("/devices/{clientName}")
abstract class TestWebSocketClient implements AutoCloseable {

  private WebSocketSession session
  private HttpRequest request
  private Collection<Object> receivedMessages = new ConcurrentLinkedQueue<>()
  private JsonSlurper jsonSlurper = new JsonSlurper()

  @OnOpen
  void onOpen(WebSocketSession session, HttpRequest request) {
    this.session = session
    this.request = request
  }

  abstract void send(String message);

  @OnMessage
  void onMessage(String message) {
    def parsedMessage = jsonSlurper.parseText(message)
    receivedMessages << parsedMessage
  }

  Collection<Object> getReceivedMessages() {
    return new ArrayList<Object>(receivedMessages)
  }
}