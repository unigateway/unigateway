import axios from "axios";
import ConfigurationReplacementResult from "./ConfigurationReplacementResult";
import {GatewayConfiguration as GatewayConfigurationData} from "./MqgatewayTypes";
import yaml from "js-yaml";
import OtherGateway from "./OtherGateway";
import GatewayStatusResource from "./GatewayStatusResource";

export default class GatewayRest {

  constructor(private readonly url: string) {}

  async sendNewConfig(newConfigurationYaml: string): Promise<ConfigurationReplacementResult> {
    const result = await axios.put(`${ this.url }/configuration/gateway`, newConfigurationYaml, {headers: {'Content-Type': "application/x-yaml"}})
    return result.data
  }

  async fetchConfiguration(): Promise<string> {
    const result = await axios(`${ this.url }/configuration/gateway`);
    return result.data
  }

  async reloadConfiguration(): Promise<{yaml: string, data: GatewayConfigurationData}> {
    const gatewayConfigurationString = await this.retry(() => this.fetchConfiguration())
    const gatewayConfigurationData: GatewayConfigurationData = yaml.load(gatewayConfigurationString) as GatewayConfigurationData;
    return { yaml: gatewayConfigurationString, data: gatewayConfigurationData }
  }

  private retry = async (refreshFunction: () => Promise<string>, numberOfTries = 90, intervalMs = 1000) => {
    let tries = 1;
    while (tries < numberOfTries) {
      try {
        return await refreshFunction()
      } catch (e) {
        tries += 1;
        await new Promise(resolve => setTimeout(resolve, intervalMs));
      }
    }
    return Promise.reject();
  }

  async fetchOtherGateways(): Promise<OtherGateway[]> {
    const result = await axios.get(`${ this.url }/discovery`)
    return result.data
  }

  async fetchStatus(): Promise<GatewayStatusResource> {
    const result = await axios.get(`${ this.url }/status`)
    return result.data
  }

}