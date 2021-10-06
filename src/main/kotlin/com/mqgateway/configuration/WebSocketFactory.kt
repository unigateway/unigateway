package com.mqgateway.configuration

import com.mqgateway.webapi.GatewayDevicesStateHandler
import com.mqgateway.webapi.GatewayWebSocketUpdateListener
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.websocket.WebSocketBroadcaster
import javax.inject.Singleton

@Factory
@Requires(property = "gateway.websocket.enabled", value = "true")
internal class WebSocketFactory {

  @Singleton
  fun gatewayDevicesStateHandler(): GatewayDevicesStateHandler {
    return GatewayDevicesStateHandler()
  }

  @Singleton
  fun gatewayWebSocketUpdateListener(broadcaster: WebSocketBroadcaster): GatewayWebSocketUpdateListener {
    return GatewayWebSocketUpdateListener(broadcaster)
  }
}
