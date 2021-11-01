import ReconnectingWebSocket from 'reconnecting-websocket';
import {CloseEvent, Event} from "reconnecting-websocket/dist/events";

class GatewayLogsWS {

  private ws?: ReconnectingWebSocket = undefined
  private initialLogsCallback: (devicesState: Log[]) => void = () => {}
  private connectedCallBack: (openEvent: Event) => void = () => {}
  private disconnectedCallBack: (closeEvent: CloseEvent) => void = () => {}
  private logCallback: (log: Log) => void = () => {}

  constructor(private readonly url: string) {}

  private connect() {
    this.ws?.close(1000, "Reconnecting")
    const server = new ReconnectingWebSocket(this.url, [], {minReconnectionDelay: 500, maxReconnectionDelay: 5000});
    server.addEventListener("open", (openEvent) => {
      this.connectedCallBack(openEvent)
    })
    server.addEventListener("close", (closeEvent) => {
      this.disconnectedCallBack(closeEvent)
    })
    server.addEventListener("message", (messageEvent) => {
      this.messageReceived(messageEvent)
    })
    this.ws = server
  }

  disconnect() {
    this.ws?.close()
  }

  private messageReceived(message: MessageEvent) {
    const messageData = JSON.parse(message.data)
    switch (messageData.type) {
      case "INITIAL_LOGS":
        const initialLogsMessage: InitialLogs = JSON.parse(message.data)
        this.initialLogsReceived(initialLogsMessage.message)
        return;
      case "LOG":
        const logMessage: LogMessage = JSON.parse(message.data)
        this.logReceived(logMessage.message)
        return;
    }
  }

  private initialLogsReceived(logs: Log[]) {
    this.initialLogsCallback(logs)
  }

  private logReceived(log: Log) {
    this.logCallback(log)
  }

  async startReadingLogs(): Promise<Log[]> {
    const promise = new Promise<Log[]>((resolve) => {
      this.initialLogsCallback = (logs) => {
        resolve(logs)
      }
    })
    if (this.ws?.readyState === ReconnectingWebSocket.OPEN) {
      this.ws.reconnect(1000, "Reconnecting to receive initial state")
    } else {
      this.connect()
    }
    return promise;
  }

  onConnected(callback: () => void) {
    this.connectedCallBack = callback
  }

  onDisconnected(callback: () => void) {
    this.disconnectedCallBack = callback
  }

  onLog(callback: (log: Log) => void) {
    this.logCallback = callback
  }
}

type InitialLogs = {
  type: string
  message: Log[]
}

type LogMessage = {
  type: string
  message: Log
}

type Log = {
  message: string
  level: string
  time: number
}

enum ConnectionState {
  CONNECTED,
  DISCONNECTED
}

export { GatewayLogsWS, ConnectionState }
export type { Log }
