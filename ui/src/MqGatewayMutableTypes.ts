import {Device as DeviceData, GatewayConfiguration as GatewayConfigurationData, DeviceType} from "./communication/MqgatewayTypes";
import {v4 as uuidv4} from 'uuid';

type PropertyChange = (deviceId: string, propertyId: string, newValue: string) => Promise<string>

class GatewayConfiguration {
  configVersion: string
  id: string
  name: string
  devices: Device[]
  isModified: boolean = false

  constructor(configVersion: string, id: string, name: string, devices: Device[]) {
    this.configVersion = configVersion
    this.id = id
    this.name = name
    this.devices = devices;
  }

  static fromData(data: GatewayConfigurationData, propertyChangeFunction: PropertyChange): GatewayConfiguration {
    return new GatewayConfiguration(data.configVersion, data.id, data.name, data.devices.map(deviceData => Device.fromData(deviceData, propertyChangeFunction)))
  }

  findDevice(deviceId: string): Device | undefined {
    return this.devices.find(device => device.id === deviceId)
  }

  findDeviceByUuid(uuid: string): Device | undefined {
    return this.devices.find(device => device.uuid === uuid)
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

  allDevices(): Device[] {
    return this.devices
  }

  hasAnyChanges(): boolean {
    return this.isModified || this.allDevices().some(device => device.isModifiedOrNewDevice)
  }

  toDataObject(): GatewayConfigurationData {
    const { isModified, ...data } = this
    return {...data, devices: this.devices.map(it => it.toDataObject())}
  }
}

class Device {
  id: string
  name: string
  type: DeviceType
  connectors: Map<string, object>
  config: Map<string, string>
  properties: Map<string, string>
  isModifiedOrNewDevice: boolean
  private propertyChangeFunction: PropertyChange

  constructor(id: string, name: string, type: DeviceType, connectors: Map<string, object>, config: Map<string, string>,
              isModifiedOrNew: boolean, propertyChangeFunction: PropertyChange, readonly uuid: string = uuidv4()) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.connectors = connectors;
    this.config = config;
    this.isModifiedOrNewDevice = isModifiedOrNew
    this.properties = new Map<string, string>()
    this.propertyChangeFunction = propertyChangeFunction
  }

  static fromData(data: DeviceData, propertyChangeFunction: PropertyChange): Device {
    return new Device(data.id, data.name, data.type, this.connectorsObjectToMap(data.connectors), this.configObjectToMap(data.config),
      false, propertyChangeFunction)
  }

  private static connectorsObjectToMap(input: object | undefined): Map<string, object> {
    if (!input) {
      return new Map<string, object>()
    }

    return new Map<string, object>(Object.entries(input))
  }

  private static configObjectToMap(input: object | undefined): Map<string, string> {
    if (!input) {
      return new Map<string, string>()
    }

    return new Map<string, string>(Object.entries(input))
  }

  changeProperty(propertyId: string, newValue: string) {
    this.propertyChangeFunction(this.id, propertyId, newValue)
  }

  handlePropertyChange(propertyId: string, newValue: string) {
    this.properties.set(propertyId, newValue)
  }

  toDataObject(): DeviceData {
    const connectors = Device.mapToObject(this.connectors)
    const config = Device.mapToObject(this.config)
    const {properties, propertyChangeFunction, isModifiedOrNewDevice, uuid, ...deviceData} = this
    return {...deviceData, connectors, config}
  }

  private static mapToObject(map: Map<any, any>) {
    if (map && map.size === 0) {
      return undefined
    }
    return Object.fromEntries(map)
  }
}

export { GatewayConfiguration, Device }
