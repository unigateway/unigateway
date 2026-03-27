import ReconnectingWebSocket from 'reconnecting-websocket'
import { useGatewayStore } from '@/store/useGatewayStore'

export interface DeviceStateUpdate {
  deviceId: string
  propertyId: string
  newValue: any
}

export interface DeviceState {
  deviceId: string
  properties: DeviceProperty[]
}

export interface DeviceProperty {
  propertyId: string
  value: string
}

interface InitialStateMessage {
  type: 'INITIAL_STATE_LIST'
  message: DeviceState[]
}

interface StateUpdateMessage {
  type: 'STATE_UPDATE'
  message: DeviceStateUpdate
}

interface AcknowledgeMessage {
  type: 'ACKNOWLEDGE'
  message: { message: string }
}

type WebSocketMessage = InitialStateMessage | StateUpdateMessage | AcknowledgeMessage

class GatewayWebSocket {
  private ws: ReconnectingWebSocket | null = null
  private messageHandlers: ((update: DeviceStateUpdate) => void)[] = []
  private initialStateCallback: ((devicesState: DeviceState[]) => void) | null = null
  private acknowledgeCallback: ((message: string) => void) | null = null

  connect(url: string) {
    if (this.ws) {
      this.ws.close()
    }

    this.ws = new ReconnectingWebSocket(url, [], {
      minReconnectionDelay: 500,
      maxReconnectionDelay: 5000,
    })

    this.ws.addEventListener('open', () => {
      console.log('WebSocket connected')
      useGatewayStore.getState().setConnectionState('CONNECTED')
    })

    this.ws.addEventListener('close', () => {
      console.log('WebSocket disconnected')
      useGatewayStore.getState().setConnectionState('DISCONNECTED')
    })

    this.ws.addEventListener('message', (event) => {
      this.handleMessage(event)
    })

    this.ws.addEventListener('error', (error) => {
      console.error('WebSocket error:', error)
    })
  }

  private handleMessage(event: MessageEvent) {
    try {
      const message: WebSocketMessage = JSON.parse(event.data)
      console.log('WebSocket message received:', message)

      switch (message.type) {
        case 'INITIAL_STATE_LIST':
          this.handleInitialState(message.message)
          break
        case 'STATE_UPDATE':
          this.handleStateUpdate(message.message)
          break
        case 'ACKNOWLEDGE':
          // Handle both nested and direct message formats
          const ackMessage = typeof message.message === 'object' && 'message' in message.message
            ? message.message.message
            : message.message
          this.handleAcknowledge(ackMessage)
          break
        default:
          console.warn('Unknown message type:', message)
      }
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error, event.data)
    }
  }

  private handleInitialState(devicesState: DeviceState[]) {
    console.log('Received initial state:', devicesState)

    // Apply initial state to all devices
    devicesState.forEach(deviceState => {
      deviceState.properties.forEach(property => {
        useGatewayStore.getState().updateDeviceProperty(
          deviceState.deviceId,
          property.propertyId,
          property.value
        )
      })
    })

    if (this.initialStateCallback) {
      this.initialStateCallback(devicesState)
      this.initialStateCallback = null
    }
  }

  private handleStateUpdate(update: DeviceStateUpdate) {
    console.log('Received state update:', update)

    // Notify handlers
    this.messageHandlers.forEach(handler => handler(update))

    // Update store
    useGatewayStore.getState().updateDeviceProperty(
      update.deviceId,
      update.propertyId,
      update.newValue
    )
  }

  private handleAcknowledge(message: any) {
    console.log('Received acknowledge:', message, typeof message)
    if (this.acknowledgeCallback) {
      // Treat undefined, null, empty string, or "OK" as success
      const ackMessage = message ?? 'OK'
      const ackString = typeof ackMessage === 'string' ? ackMessage : String(ackMessage)
      this.acknowledgeCallback(ackString || 'OK')
      this.acknowledgeCallback = null
    }
  }

  async fetchInitialState(): Promise<DeviceState[]> {
    return new Promise((resolve) => {
      this.initialStateCallback = (devicesState) => {
        resolve(devicesState)
      }

      // Reconnect to trigger initial state message
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.reconnect(1000, 'Reconnecting to receive initial state')
      }
    })
  }

  onMessage(handler: (update: DeviceStateUpdate) => void) {
    this.messageHandlers.push(handler)
  }

  async updateState(deviceId: string, propertyId: string, newValue: any): Promise<string> {
    return new Promise((resolve, reject) => {
      // Set timeout in case we never get an acknowledgment
      const timeout = setTimeout(() => {
        this.acknowledgeCallback = null
        reject(new Error('Timeout waiting for acknowledgment'))
      }, 5000)

      this.acknowledgeCallback = (message: string) => {
        clearTimeout(timeout)
        // Accept 'OK', empty string, or undefined as success
        if (!message || message === 'OK' || message === 'undefined') {
          resolve('OK')
        } else {
          reject(new Error(message))
        }
      }

      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ deviceId, propertyId, newValue }))
      } else {
        clearTimeout(timeout)
        reject(new Error('WebSocket not connected'))
      }
    })
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }
}

export const gatewayWebSocket = new GatewayWebSocket()
