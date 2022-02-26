package com.mqgateway.webapi

import ch.qos.logback.classic.spi.ILoggingEvent
import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import java.util.LinkedList

class LogsWebSocketPublisher(private val broadcaster: WebSocketBroadcaster) {

  private val lastLogs: LinkedList<GatewayLogsServerWebSocket.Log> = LinkedList()

  fun sendLastLogs(session: WebSocketSession) {
    session.sendSync(GatewayLogsServerWebSocket.InitialLogsMessage(lastLogs))
  }

  fun onLogEvent(event: ILoggingEvent) {
    if (lastLogs.size >= LAST_LOGS_SIZE) {
      lastLogs.poll()
    }
    val log = GatewayLogsServerWebSocket.Log(event.message, event.timeStamp, event.level.toString(), event.loggerName)
    lastLogs.offer(log)
    broadcaster.broadcastSync(
      GatewayLogsServerWebSocket.LogMessage(log)
    ) { it.attributes.getValue("type") == "logs" }
  }

  companion object {
    private const val LAST_LOGS_SIZE = 1000
  }
}
