type GatewayConfiguration = {
  configVersion: string
  id: string
  name: string
  devices: Device[]
}

type Device = {
  id: string
  name: string
  type: DeviceType
  connectors: object[]
  config?: object
  internalDevices?: object
  referencedDeviceId?: string
}

enum DeviceType {
  UNIGATEWAY,
  RELAY,
  SWITCH_BUTTON,
  REED_SWITCH,
  TEMPERATURE,
  HUMIDITY,
  MOTION_DETECTOR,
  EMULATED_SWITCH,
  TIMER_SWITCH,
  SHUTTER,
  GATE,
}

export { DeviceType }
export type { GatewayConfiguration, Device }
