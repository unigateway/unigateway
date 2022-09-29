type GatewayConfiguration = {
  configVersion: string
  name: string
  mqttHostname: string
  rooms: Room[]
}

type Room = {
  name: string
  points: Point[]
}

type Point = {
  name: string
  portNumber: number
  devices: Device[]
}

type Device = {
  id: string
  name: string
  type: DeviceType
  wires?: WireColor[]
  config?: object
  internalDevices?: object
  referencedDeviceId?: string
}

enum DeviceType {
  MQGATEWAY,
  REFERENCE,
  RELAY,
  SWITCH_BUTTON,
  REED_SWITCH,
  BME280,
  SCT013,
  DHT22,
  MOTION_DETECTOR,
  EMULATED_SWITCH,
  TIMER_SWITCH,
  SHUTTER,
  GATE,
}

enum WireColor {
  ORANGE_WHITE = 1,
  ORANGE = 2,
  GREEN_WHITE = 3,
  BLUE = 4,
  BLUE_WHITE = 5,
  GREEN = 6,
  BROWN_WHITE = 7,
  BROWN = 8,
}

export {DeviceType, WireColor}
export type { GatewayConfiguration, Room, Device, Point }
