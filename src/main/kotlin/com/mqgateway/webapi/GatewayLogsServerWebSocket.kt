package com.mqgateway.webapi

import com.mqgateway.webapi.GatewayServerWebSocket.MessageType.INITIAL_LOGS
import com.mqgateway.webapi.GatewayServerWebSocket.MessageType.LOG
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket

private val LOGGER = KotlinLogging.logger {}

@ServerWebSocket("/logs/{clientId}")
class GatewayLogsServerWebSocket(private val logsWebSocketPublisher: LogsWebSocketPublisher) {
  @OnOpen
  fun onOpen(
    clientId: String,
    session: WebSocketSession,
  ) {
    LOGGER.info { "Client $clientId connected" }
    session.attributes.put("type", "logs")
    logsWebSocketPublisher.sendLastLogs(session)
  }

  @OnMessage
  fun onMessage(
    clientId: String,
    message: String,
    @Suppress("UNUSED_PARAMETER") session: WebSocketSession,
  ) {
    LOGGER.info { "Message from client: '$clientId' with content $message" }
  }

  @OnClose
  fun onClose(
    @Suppress("UNUSED_PARAMETER") clientId: String,
  ) {
  }

  @Serdeable
  data class Log(val message: String, val time: Long, val level: String, val loggerName: String)

  class LogMessage(log: Log) : GatewayServerWebSocket.MessageWrapper<Log>(LOG, log)

  class InitialLogsMessage(initialLogs: List<Log>) : GatewayServerWebSocket.MessageWrapper<List<Log>>(INITIAL_LOGS, initialLogs)
}
