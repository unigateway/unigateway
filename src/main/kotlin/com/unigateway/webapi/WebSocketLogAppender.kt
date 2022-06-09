package com.unigateway.webapi

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import org.slf4j.LoggerFactory
import javax.annotation.PreDestroy

class WebSocketLogAppender(private val logsWebSocketPublisher: LogsWebSocketPublisher) :
  AppenderBase<ILoggingEvent>(), ApplicationEventListener<StartupEvent> {

  override fun append(eventObject: ILoggingEvent) {
    logsWebSocketPublisher.onLogEvent(eventObject)
  }

  override fun onApplicationEvent(event: StartupEvent?) {
    this.context = LoggerFactory.getILoggerFactory() as LoggerContext
    val root: Logger = LoggerFactory.getLogger(UNIGATEWAY_LOGGER_NAME) as Logger
    root.addAppender(this)
    this.start()
  }

  @PreDestroy
  fun close() {
    this.stop()
  }

  companion object {
    const val UNIGATEWAY_LOGGER_NAME = "com.unigateway"
  }
}
