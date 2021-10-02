import GatewayRest from "./GatewayRest";
import {GatewayWS} from "./GatewayWS";
import {GatewayConfiguration} from "../MqGatewayMutableTypes";
import ConfigurationReplacementResult from "./ConfigurationReplacementResult";
import OtherGateway from "./OtherGateway";


export default class GatewayConnection {
  constructor(readonly rest: GatewayRest, readonly ws: GatewayWS) {
  }

  async reconnect(): Promise<{yaml: string, gatewayConfiguration: GatewayConfiguration}> {
    const reloadValues = await Promise.all([this.rest.reloadConfiguration(), this.ws.fetchInitialState()])
    const { yaml, data } = reloadValues[0]
    const devicesInitialState = reloadValues[1]
    const gatewayConfiguration = GatewayConfiguration.fromData(data,
      (deviceId, propertyId, newValue) => this.ws.updateState(deviceId, propertyId, newValue))

    devicesInitialState.forEach(initialState => {
      const device = gatewayConfiguration.findDevice(initialState.deviceId)
      initialState.properties.forEach(property => {
        if (device) {
          device.handlePropertyChange(property.propertyId, property.value)
        }
      })
    })
    return {yaml, gatewayConfiguration}
  }

  async sendNewConfig(newConfigurationYaml: string): Promise<ConfigurationReplacementResult> {
    return await this.rest.sendNewConfig(newConfigurationYaml)
  }

  async fetchOtherGateways(): Promise<OtherGateway[]> {
    return await this.rest.fetchOtherGateways()
  }
}