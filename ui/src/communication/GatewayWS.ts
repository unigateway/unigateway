import ReconnectingWebSocket from 'reconnecting-websocket';
import {CloseEvent, Event} from "reconnecting-websocket/dist/events";

class GatewayWS {

  private ws?: ReconnectingWebSocket = undefined
  private acknowledgeCallback: (message: string) => void = () => {}
  private initialStateCallback: (devicesState: DeviceState[]) => void = () => {}
  private updateCallback: (update: DeviceStateUpdate) => void = () => {}
  private connectedCallBack: (openEvent: Event) => void = () => {}
  private disconnectedCallBack: (closeEvent: CloseEvent) => void = () => {}

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

  async updateState(deviceId: string, propertyId: string, newValue: string): Promise<string> {
    const promise = new Promise<string>((resolve, reject) => {
      this.acknowledgeCallback = (message: string) => {
        if (message === "OK") {
          resolve(message)
        } else {
          reject(message)
        }
      }
    })
    if (this.ws) {
      this.ws.send(JSON.stringify(new DeviceStateChangeMessage(deviceId, propertyId, newValue)))
      return promise
    } else {
      return Promise.reject("WS not initialized")
    }
  }

  private messageReceived(message: MessageEvent) {
    const messageData = JSON.parse(message.data)
    switch (messageData.type) {
      case "INITIAL_STATE_LIST":
        const initialStateMessage: InitialStateMessage = JSON.parse(message.data)
        this.initialStateListReceived(initialStateMessage.message)
        return;
      case "ACKNOWLEDGE":
        this.acknowledgeMessageReceived(messageData.message.message)
        return;
      case "STATE_UPDATE":
        const updateMessage: DeviceStateUpdateMessage = JSON.parse(message.data)
        this.stateUpdateReceived(updateMessage.message)
        return;
    }
  }

  private initialStateListReceived(devicesState: DeviceState[]) {
    this.initialStateCallback(devicesState)
  }

  private acknowledgeMessageReceived(message: string) {
    this.acknowledgeCallback(message)
  }

  private stateUpdateReceived(update: DeviceStateUpdate) {
    this.updateCallback(update)
  }

  async fetchInitialState(): Promise<DeviceState[]> {
    const promise = new Promise<DeviceState[]>((resolve) => {
      this.initialStateCallback = (devicesState) => {
        resolve(devicesState)
      }
    })
    if (this.ws?.readyState === ReconnectingWebSocket.OPEN) {
      this.ws.reconnect(1000, "Reconnecting to receive initial state")
    } else {
      this.connect()
    }
    return promise;
  }

  onUpdate(callback: (update: DeviceStateUpdate) => void) {
    this.updateCallback = callback
  }

  onConnected(callback: () => void) {
    this.connectedCallBack = callback
  }

  onDisconnected(callback: () => void) {
    this.disconnectedCallBack = callback
  }
}

type InitialStateMessage = {
  type: string
  message: DeviceState[]
}

type DeviceState = {
  deviceId: string
  properties: DeviceProperty[]
}

type DeviceProperty = {
  propertyId: string
  value: string
}

type DeviceStateUpdateMessage = {
  type: string
  message: DeviceStateUpdate
}

type DeviceStateUpdate = {
  deviceId: string
  propertyId: string
  newValue: string
}

class DeviceStateChangeMessage {
  constructor(readonly deviceId: string, readonly propertyId: string, readonly newValue: string) {}
}

enum ConnectionState {
  CONNECTED,
  DISCONNECTED
}

export { GatewayWS, ConnectionState }
export type { DeviceState, DeviceProperty, DeviceStateUpdate }
