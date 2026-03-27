import ReconnectingWebSocket from 'reconnecting-websocket'

export interface Log {
  message: string
  level: string
  time: number
}

interface InitialLogsMessage {
  type: 'INITIAL_LOGS'
  message: Log[]
}

interface LogMessage {
  type: 'LOG'
  message: Log
}

type LogsWebSocketMessage = InitialLogsMessage | LogMessage

class GatewayLogsWebSocket {
  private ws: ReconnectingWebSocket | null = null
  private initialLogsCallback: ((logs: Log[]) => void) | null = null
  private logCallback: ((log: Log) => void) | null = null

  connect(url: string) {
    if (this.ws) {
      this.ws.close()
    }

    this.ws = new ReconnectingWebSocket(url, [], {
      minReconnectionDelay: 500,
      maxReconnectionDelay: 5000,
    })

    this.ws.addEventListener('open', () => {
      console.log('Logs WebSocket connected')
    })

    this.ws.addEventListener('close', () => {
      console.log('Logs WebSocket disconnected')
    })

    this.ws.addEventListener('message', (event) => {
      this.handleMessage(event)
    })

    this.ws.addEventListener('error', (error) => {
      console.error('Logs WebSocket error:', error)
    })
  }

  private handleMessage(event: MessageEvent) {
    try {
      const message: LogsWebSocketMessage = JSON.parse(event.data)

      switch (message.type) {
        case 'INITIAL_LOGS':
          this.handleInitialLogs(message.message)
          break
        case 'LOG':
          this.handleLog(message.message)
          break
        default:
          console.warn('Unknown logs message type:', message)
      }
    } catch (error) {
      console.error('Failed to parse logs WebSocket message:', error, event.data)
    }
  }

  private handleInitialLogs(logs: Log[]) {
    console.log('Received initial logs:', logs.length)
    if (this.initialLogsCallback) {
      this.initialLogsCallback(logs)
      this.initialLogsCallback = null
    }
  }

  private handleLog(log: Log) {
    if (this.logCallback) {
      this.logCallback(log)
    }
  }

  async startReadingLogs(): Promise<Log[]> {
    return new Promise((resolve) => {
      this.initialLogsCallback = (logs) => {
        resolve(logs)
      }

      // Reconnect to trigger initial logs message
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.reconnect(1000, 'Reconnecting to receive initial logs')
      }
    })
  }

  onLog(callback: (log: Log) => void) {
    this.logCallback = callback
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.initialLogsCallback = null
    this.logCallback = null
  }
}

export const gatewayLogsWebSocket = new GatewayLogsWebSocket()
