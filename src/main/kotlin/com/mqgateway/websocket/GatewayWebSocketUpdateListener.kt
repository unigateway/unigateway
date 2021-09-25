package com.mqgateway.websocket

import com.mqgateway.core.device.UpdateListener
import io.micronaut.websocket.WebSocketBroadcaster

class GatewayWebSocketUpdateListener(private val broadcaster: WebSocketBroadcaster) : UpdateListener {

  override fun valueUpdated(deviceId: String, propertyId: String, newValue: String) {
    broadcaster.broadcastSync(
      GatewayServerWebSocket.MessageWrapper(
        GatewayServerWebSocket.MessageType.STATE_UPDATE,
        UpdateMessage(deviceId, propertyId, newValue)
      )
    )
  }

  data class UpdateMessage(val deviceId: String, val propertyId: String, val newValue: String)
}
