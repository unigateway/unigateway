package com.mqgateway.webapi

import com.fasterxml.jackson.annotation.JsonCreator
import com.mqgateway.core.device.DeviceRegistry
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnError
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import org.reactivestreams.Publisher

private val LOGGER = KotlinLogging.logger {}

@ServerWebSocket("/devices/{clientId}")
class GatewayServerWebSocket(private val gatewayDeviceRegistry: DeviceRegistry, private val devicesStateHandler: GatewayDevicesStateHandler) {
  @OnOpen
  fun onOpen(
    clientId: String,
    session: WebSocketSession,
  ): Publisher<MessageWrapper<List<DeviceState>>> {
    LOGGER.info { "Client $clientId connected" }
    session.attributes.put("type", "devices")
    return session.send(MessageWrapper(MessageType.INITIAL_STATE_LIST, devicesStateHandler.devicesState()))
  }

  @OnMessage
  fun onMessage(
    clientId: String,
    message: DeviceChangeMessage,
    session: WebSocketSession,
  ): Publisher<MessageWrapper<String>> {
    LOGGER.debug { "Received message from '$clientId': $message" }
    return gatewayDeviceRegistry.getById(message.deviceId)?.let {
      it.change(message.propertyId, message.newValue)
      return session.send(OkResponseMessage())
    } ?: session.send(DeviceNotFoundError(message.deviceId))
  }

  @OnClose
  fun onClose(clientId: String) {
    LOGGER.info { "Client $clientId closed the connection" }
  }

  @OnError
  fun onError(
    clientId: String,
    exception: Throwable,
    session: WebSocketSession,
  ): Publisher<GeneralError> {
    LOGGER.error { "WebSocket client '$clientId' exception: $exception" }
    return session.send(GeneralError(exception.message ?: "Unknown error, check logs for more information"))
  }

  @Serdeable.Deserializable
  @Introspected
  data class DeviceChangeMessage
    @JsonCreator
    constructor(val deviceId: String, val propertyId: String, val newValue: String)

  class DeviceNotFoundError(deviceId: String) : MessageWrapper<String>(MessageType.REJECT, "Device with id '$deviceId' not found")

  class OkResponseMessage : MessageWrapper<String>(MessageType.ACKNOWLEDGE, "OK")

  class GeneralError(message: String) : MessageWrapper<String>(MessageType.ERROR, message)

  @Serdeable
  open class MessageWrapper<T>(val type: MessageType, val message: T) {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as MessageWrapper<*>

      if (type != other.type) return false
      if (message != other.message) return false

      return true
    }

    override fun hashCode(): Int {
      var result = type.hashCode()
      result = 31 * result + (message?.hashCode() ?: 0)
      return result
    }
  }

  enum class MessageType {
    INITIAL_STATE_LIST,
    ACKNOWLEDGE,
    REJECT,
    ERROR,
    STATE_UPDATE,
    INITIAL_LOGS,
    LOG,
  }
}
