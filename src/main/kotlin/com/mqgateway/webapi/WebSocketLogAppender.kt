package com.mqgateway.webapi

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.LoggerFactory
import javax.annotation.PreDestroy
import javax.inject.Singleton

@Singleton
class WebSocketLogAppender(private val logsWebSocketPublisher: LogsWebSocketPublisher) : AppenderBase<ILoggingEvent>() {

  override fun append(eventObject: ILoggingEvent) {
    logsWebSocketPublisher.onLogEvent(eventObject)
  }

  fun init() {
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
