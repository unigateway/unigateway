import {Device as DeviceData, Point as PointData, Room as RoomData, GatewayConfiguration as GatewayConfigurationData, DeviceType, WireColor} from "./communication/MqgatewayTypes";
import {v4 as uuidv4} from 'uuid';

type PropertyChange = (deviceId: string, propertyId: string, newValue: string) => Promise<string>

class GatewayConfiguration {
  configVersion: string
  name: string
  mqttHostname: string
  rooms: Room[]
  isModified: boolean = false

  constructor(configVersion: string, name: string, mqttHostname: string, rooms: Room[]) {
    this.configVersion = configVersion
    this.name = name
    this.mqttHostname = mqttHostname
    this.rooms = rooms;
  }

  static fromData(data: GatewayConfigurationData, propertyChangeFunction: PropertyChange): GatewayConfiguration {
    return new GatewayConfiguration(data.configVersion, data.name, data.mqttHostname, data.rooms.map(roomData => Room.fromData(roomData, propertyChangeFunction)))
  }

  deviceLocation(deviceUuid: string): {room: Room, point: Point} | null {
    const room = this.rooms.find(room => room.points.flatMap(point => point.devices).find(device => device.uuid === deviceUuid))
    const point = room?.points.find(point => point.devices.find(device => device.uuid === deviceUuid))
    if (room && point) {
      return {room, point}
    } else {
      return null
    }
  }

  moveDeviceToPoint(deviceUuid: string, newRoomName: string, newPointName: string): boolean {
    let moved = false
    const deviceToBeMoved = this.findDeviceByUuid(deviceUuid)!
    this.rooms.flatMap(room => room.points).forEach(point => {
      const deviceIndex = point.devices.indexOf(deviceToBeMoved)
      if (deviceIndex !== -1) {
        point.devices.splice(deviceIndex, 1)
        this.getRoom(newRoomName).getPoint(newPointName).devices.push(deviceToBeMoved)
        moved = true
      }
    })
    return moved
  }

  getRoom(roomName: string): Room {
    return this.rooms.find(it => it.name === roomName)!
  }

  findRoomByUuid(uuid: string): Room | undefined {
    return this.rooms.find(room => room.uuid === uuid)
  }

  addRoom(room: Room) {
    if (this.getRoom(room.name)) {
      throw new Error("Room with this name already exists")
    }
    this.rooms.push(room)
  }

  deleteRoom(roomUuid: string) {
    const roomIndex = this.rooms.findIndex(it => it.uuid === roomUuid)
    this.rooms.splice(roomIndex, 1)
    this.isModified = true
  }

  findPointByUuid(uuid: string): Point | undefined {
    return this.rooms.flatMap(room => room.points).find(point => point.uuid === uuid)
  }

  pointLocation(pointUuid: string): Room | null {
    const room = this.rooms.find(room => room.points.find(point => point.uuid === pointUuid))
    if (room) {
      return room
    } else {
      return null
    }
  }

  findDevice(deviceId: string): Device | undefined {
    return this.rooms.flatMap(room => room.points).flatMap(point => point.devices).find(device => device.id === deviceId)
  }

  findDeviceByUuid(uuid: string): Device | undefined {
    return this.rooms.flatMap(room => room.points).flatMap(point => point.devices).find(device => device.uuid === uuid)
  }

  replaceDevice(device: Device) {
    const {point} = this.deviceLocation(device.uuid)!
    point.replaceDevice(device)
  }

  deleteDevice(device: Device) {
    const {point} = this.deviceLocation(device.uuid)!
    point.deleteDevice(device)
  }

  allDevices(): Device[] {
    return this.rooms.flatMap(room => room.points).flatMap(point => point.devices)
  }

  hasAnyChanges(): boolean {
    const points = this.rooms.flatMap(room => room.points)
    return this.isModified || this.allDevices().some(device => device.isModifiedOrNewDevice) || points.some(point => point.hasAnyChanges())
  }

  toDataObject(): GatewayConfigurationData {
    const { isModified, ...data } = this
    return {...data, rooms: this.rooms.map(it => it.toDataObject())}
  }
}

class Room {
  name: string
  points: Point[]

  constructor(name: string, points: Point[], readonly uuid: string = uuidv4()) {
    this.name = name;
    this.points = points;
  }

  static fromData(data: RoomData, propertyChangeFunction: PropertyChange): Room {
    return new Room(data.name, data.points.map(pointData => Point.fromData(pointData, propertyChangeFunction)))
  }

  getPoint(pointName: string) {
    return this.points.find(it => it.name === pointName)!
  }

  addPoint(point: Point) {
    if (this.getPoint(point.name)) {
      throw new Error("Point with this name already exists in this room")
    }
    this.points.push(point)
  }

  deletePoint(pointUuid: string) {
    const pointIndex = this.points.findIndex(it => it.uuid === pointUuid)
    this.points.splice(pointIndex, 1)
  }

  toDataObject(): RoomData {
    return {name: this.name, points: this.points.map(it => it.toDataObject())}
  }
}

class Point {
  name: string
  portNumber: number
  devices: Device[]
  isModified: boolean = false

  constructor(name: string, portNumber: number, devices: Device[], readonly uuid: string = uuidv4()) {
    this.name = name;
    this.portNumber = portNumber;
    this.devices = devices;
  }

  static fromData(data: PointData, propertyChangeFunction: PropertyChange): Point {
    return new Point(data.name, data.portNumber, data.devices.map(deviceData => Device.fromData(deviceData, propertyChangeFunction)))
  }

  replaceDevice(device: Device) {
    const deviceIndex = this.devices.findIndex(it => it.uuid === device.uuid)
    this.devices[deviceIndex] = device
  }

  deleteDevice(device: Device) {
    const deviceIndex = this.devices.findIndex(it => it.uuid === device.uuid)
    this.devices.splice(deviceIndex, 1)
    this.isModified = true
  }

  hasAnyChanges() {
    return this.isModified
  }

  addDevice(device: Device) {
    this.devices.push(device)
  }

  toDataObject(): PointData {
    return {name: this.name, portNumber: this.portNumber, devices: this.devices.map(it => it.toDataObject())}
  }
}

class Device {
  id: string
  name: string
  type: DeviceType
  wires: WireColor[]
  config: Map<string, string>
  internalDevices: Map<string, Device>
  referencedDeviceId: string | null
  properties: Map<string, string>
  isModifiedOrNewDevice: boolean
  private propertyChangeFunction: PropertyChange

  constructor(id: string, name: string, type: DeviceType, wires: WireColor[], config: Map<string, string>, internalDevices: Map<string, Device>,
              referencedDeviceId: string | null, isModifiedOrNew: boolean, propertyChangeFunction: PropertyChange, readonly uuid: string = uuidv4()) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.wires = wires;
    this.config = config;
    this.internalDevices = internalDevices;
    this.referencedDeviceId = referencedDeviceId;
    this.isModifiedOrNewDevice = isModifiedOrNew
    this.properties = new Map<string, string>()
    this.propertyChangeFunction = propertyChangeFunction
  }

  static fromData(data: DeviceData, propertyChangeFunction: PropertyChange): Device {
    return new Device(data.id, data.name, data.type, Object.assign([], data.wires), this.configObjectToMap(data.config),
      this.internalDevicesObjectToMap(data.internalDevices), data.referencedDeviceId || null, false, propertyChangeFunction)
  }

  private static configObjectToMap(input: object | undefined): Map<string, string> {
    if (!input) {
      return new Map<string, string>()
    }

    return new Map<string, string>(Object.entries(input))
  }

  private static internalDevicesObjectToMap(input: object | undefined): Map<string, Device> {
    if (!input) {
      return new Map<string, Device>()
    }

    const convertedEntries: [string, Device][] = Object.entries(input).map(entry => {
      const internalDeviceData: DeviceData = entry[1]
      const internalDevice: Device = this.fromData(internalDeviceData, () => Promise.reject())
      return [entry[0], internalDevice]
    })

    return new Map<string, Device>(convertedEntries)
  }

  changeProperty(propertyId: string, newValue: string) {
    this.propertyChangeFunction(this.id, propertyId, newValue)
  }

  handlePropertyChange(propertyId: string, newValue: string) {
    this.properties.set(propertyId, newValue)
  }

  toDataObject(): DeviceData {
    const referencedDeviceId: string | undefined = this.referencedDeviceId || undefined
    const internalDevices = Device.mapToObject(
      new Map(
        Array.from(this.internalDevices).map(([key, device]) => {
            return [key, device.toDataObject()]
          }
        )
      )
    )
    let wires: WireColor[] | undefined = this.wires
    if (wires.length === 0) {
      wires = undefined
    }
    const config = Device.mapToObject(this.config)
    const {properties, propertyChangeFunction, isModifiedOrNewDevice, uuid, ...deviceData} = this
    return {...deviceData, referencedDeviceId, internalDevices, config, wires}
  }

  private static mapToObject(map: Map<any, any>) {
    if (map && map.size === 0) {
      return undefined
    }
    return Object.fromEntries(map)
  }
}

export { GatewayConfiguration, Room, Device, Point }
