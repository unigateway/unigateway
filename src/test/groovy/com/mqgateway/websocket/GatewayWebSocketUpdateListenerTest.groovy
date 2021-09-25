package com.mqgateway.websocket

import io.micronaut.http.MediaType
import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate
import kotlin.NotImplementedError
import org.reactivestreams.Publisher
import spock.lang.Specification
import spock.lang.Subject

class GatewayWebSocketUpdateListenerTest extends Specification {

  WebSocketBroadcasterStub webSocketBroadcasterStub = new WebSocketBroadcasterStub()

  @Subject
  GatewayWebSocketUpdateListener listener = new GatewayWebSocketUpdateListener(webSocketBroadcasterStub)

  def "should broadcast WebSocket message when any value has been updated"() {
    given:
    def deviceId = "someDevice1"
    def propertyId = "someProperty1"
    def newValue = "newValue1"

    when:
    listener.valueUpdated(deviceId, propertyId, newValue)

    then:
    webSocketBroadcasterStub.publishedMessages.contains(updateMessage(deviceId, propertyId, newValue))
  }

  private static updateMessage(String deviceId, String propertyId, String value) {
    return new GatewayServerWebSocket.MessageWrapper(
      GatewayServerWebSocket.MessageType.STATE_UPDATE, new GatewayWebSocketUpdateListener.UpdateMessage(deviceId, propertyId, value))
  }
}

class WebSocketBroadcasterStub implements WebSocketBroadcaster {

  List publishedMessages = new ArrayList()

  @Override
  <T> Publisher<T> broadcast(T message, MediaType mediaType, Predicate<WebSocketSession> filter) {
    throw new NotImplementedError()
  }

  @Override
  <T> Publisher<T> broadcast(T message, MediaType mediaType) {
    throw new NotImplementedError()
  }

  @Override
  <T> Publisher<T> broadcast(T message) {
    throw new NotImplementedError()
  }

  @Override
  <T> Publisher<T> broadcast(T message, Predicate<WebSocketSession> filter) {
    throw new NotImplementedError()
  }

  @Override
  <T> CompletableFuture<T> broadcastAsync(T message, MediaType mediaType, Predicate<WebSocketSession> filter) {
    throw new NotImplementedError()
  }

  @Override
  <T> CompletableFuture<T> broadcastAsync(T message) {
    throw new NotImplementedError()
  }

  @Override
  <T> CompletableFuture<T> broadcastAsync(T message, Predicate<WebSocketSession> filter) {
    throw new NotImplementedError()
  }

  @Override
  <T> CompletableFuture<T> broadcastAsync(T message, MediaType mediaType) {
    throw new NotImplementedError()
  }

  @Override
  <T> void broadcastSync(T message, MediaType mediaType, Predicate<WebSocketSession> filter) {
    throw new NotImplementedError()
  }

  @Override
  <T> void broadcastSync(T message) {
    publishedMessages.add(message)
  }

  @Override
  <T> void broadcastSync(T message, Predicate<WebSocketSession> filter) {
    throw new NotImplementedError()
  }

  @Override
  <T> void broadcastSync(T message, MediaType mediaType) {
    this.broadcastSync(message)
  }
}