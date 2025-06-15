package com.mqgateway.webapi

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory

class WebSocketLogAppender(private val logsWebSocketPublisher: LogsWebSocketPublisher) :
  AppenderBase<ILoggingEvent>(), ApplicationEventListener<StartupEvent> {
  override fun append(eventObject: ILoggingEvent) {
    logsWebSocketPublisher.onLogEvent(eventObject)
  }

  override fun onApplicationEvent(event: StartupEvent?) {
    this.context = LoggerFactory.getILoggerFactory() as LoggerContext
    val root: Logger = LoggerFactory.getLogger(MQGATEWAY_LOGGER_NAME) as Logger
    root.addAppender(this)
    this.start()
  }

  @PreDestroy
  fun close() {
    this.stop()
  }

  companion object {
    const val MQGATEWAY_LOGGER_NAME = "com.mqgateway"
  }
}
