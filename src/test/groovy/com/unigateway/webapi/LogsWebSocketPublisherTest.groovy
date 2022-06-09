package com.unigateway.webapi

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.value.MutableConvertibleValues
import io.micronaut.http.MediaType
import io.micronaut.websocket.CloseReason
import io.micronaut.websocket.WebSocketSession
import java.time.Instant
import java.util.concurrent.CompletableFuture
import org.reactivestreams.Publisher
import spock.lang.Specification
import spock.lang.Subject

class LogsWebSocketPublisherTest extends Specification {

  private WebSocketBroadcasterStub webSocketBroadcasterStub = new WebSocketBroadcasterStub()

  @Subject
  LogsWebSocketPublisher publisher = new LogsWebSocketPublisher(webSocketBroadcasterStub)

  def "should broadcast WebSocket message on LogEvent"() {
    given:
    def loggingEvent = loggingEvent("Log message 123", Instant.now().toEpochMilli(), Level.INFO, "com.unigateway.some.logger.name")

    when:
    publisher.onLogEvent(loggingEvent)

    then:
    def expectedLog = new GatewayLogsServerWebSocket.Log(loggingEvent.message, loggingEvent.timeStamp, loggingEvent.level.toString(), loggingEvent.loggerName)
    webSocketBroadcasterStub.publishedMessages == [
      new GatewayLogsServerWebSocket.LogMessage(expectedLog)
    ]
  }

  def "should only keep 1000 log events and delete oldest when more then that has been logged"() {
    given:
    def nowInMillis = Instant.now().toEpochMilli()
    def loggingEvents = (1..1200).collect {
      loggingEvent("Some message $it", nowInMillis + it, Level.INFO, "com.unigateway.some.test")
    }
    def sessionStub = new WebSocketSessionStub()

    when:
    loggingEvents.each { publisher.onLogEvent(it) }
    publisher.sendLastLogs(sessionStub)

    then:
    sessionStub.publishedMessages == [
      new GatewayLogsServerWebSocket.InitialLogsMessage(
        loggingEvents.subList(200, 1200).collect {
          new GatewayLogsServerWebSocket.Log(it.message, it.timeStamp, it.level.toString(), it.loggerName)
        })
    ]
  }

  private static LoggingEvent loggingEvent(String message, long timestamp, Level level, String loggerName) {
    def loggingEvent = new LoggingEvent()
    loggingEvent.message = message
    loggingEvent.timeStamp = timestamp
    loggingEvent.level = level
    loggingEvent.loggerName = loggerName
    return loggingEvent
  }
}

class WebSocketSessionStub implements WebSocketSession {

  List publishedMessages = new ArrayList()

  @Override
  String getId() {
    return null
  }

  @Override
  MutableConvertibleValues<Object> getAttributes() {
    return null
  }

  @Override
  boolean isOpen() {
    return false
  }

  @Override
  boolean isWritable() {
    return false
  }

  @Override
  boolean isSecure() {
    return false
  }

  @Override
  Set<? extends WebSocketSession> getOpenSessions() {
    return null
  }

  @Override
  URI getRequestURI() {
    return null
  }

  @Override
  String getProtocolVersion() {
    return null
  }

  @Override
  <T> Publisher<T> send(T message, MediaType mediaType) {
    return null
  }

  @Override
  <T> CompletableFuture<T> sendAsync(T message, MediaType mediaType) {
    publishedMessages.add(message)

    def future = new CompletableFuture<T>()
    future.complete(message)
    return future
  }

  @Override
  void close() {

  }

  @Override
  void close(CloseReason closeReason) {

  }

  @Override
  MutableConvertibleValues<Object> put(CharSequence key, @Nullable Object value) {
    return null
  }

  @Override
  MutableConvertibleValues<Object> remove(CharSequence key) {
    return null
  }

  @Override
  MutableConvertibleValues<Object> clear() {
    return null
  }

  @Override
  Set<String> names() {
    return null
  }

  @Override
  Collection<Object> values() {
    return null
  }

  @Override
  <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
    return null
  }
}
