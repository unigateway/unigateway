package com.mqgateway.configuration

import com.mqgateway.webapi.GatewayDevicesStateHandler
import com.mqgateway.webapi.GatewayWebSocketUpdateListener
import com.mqgateway.webapi.LogsWebSocketPublisher
import com.mqgateway.webapi.WebSocketLogAppender
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.websocket.WebSocketBroadcaster
import jakarta.inject.Singleton

@Factory
@Requires(property = "gateway.websocket.enabled", value = "true")
internal class WebSocketFactory {

  @Singleton
  fun logsWebSocketPublisher(broadcaster: WebSocketBroadcaster):  LogsWebSocketPublisher {
    return LogsWebSocketPublisher(broadcaster)
  }

  @Singleton
  fun webSocketLogAppender(logsWebSocketPublisher: LogsWebSocketPublisher): WebSocketLogAppender {
    return WebSocketLogAppender(logsWebSocketPublisher)
  }

  @Singleton
  fun gatewayDevicesStateHandler(): GatewayDevicesStateHandler {
    return GatewayDevicesStateHandler()
  }

  @Singleton
  fun gatewayWebSocketUpdateListener(broadcaster: WebSocketBroadcaster): GatewayWebSocketUpdateListener {
    return GatewayWebSocketUpdateListener(broadcaster)
  }
}
