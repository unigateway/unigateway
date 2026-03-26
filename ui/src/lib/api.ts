import axios from 'axios'
import yaml from 'js-yaml'
import type { GatewayConfiguration } from '@/store/useGatewayStore'

export interface GatewayStatus {
  cpuTemperature: number
  freeMemoryBytes: number
  uptimeSeconds: number
  ipAddress: string
  mqttConnected: boolean
  firmwareVersion: string
  mySensorsEnabled: boolean
  expanderEnabled: boolean
  mqGatewayLatestVersion: ReleaseInfo
}

export interface ReleaseInfo {
  tag_name: string
  html_url: string
}

const api = axios.create({
  baseURL: '',
})

export interface GatewayConfigurationData {
  configVersion: string
  id: string
  name: string
  devices: any[]
}

export const gatewayApi = {
  async getConfiguration(): Promise<{ yaml: string; config: GatewayConfiguration }> {
    try {
      const response = await api.get('/configuration/gateway')
      const yamlText = response.data
      const data = yaml.load(yamlText) as GatewayConfigurationData

      const config: GatewayConfiguration = {
        configVersion: data.configVersion,
        id: data.id,
        name: data.name,
        devices: data.devices.map(d => ({
          id: d.id,
          name: d.name,
          type: d.type,
          properties: new Map(Object.entries(d.config || {})),
        })),
      }

      return { yaml: yamlText, config }
    } catch (error) {
      console.error('Failed to fetch configuration:', error)
      throw error
    }
  },

  async saveConfiguration(yamlConfig: string): Promise<void> {
    try {
      await api.put('/configuration/gateway', yamlConfig, {
        headers: { 'Content-Type': 'application/x-yaml' },
      })
    } catch (error) {
      console.error('Failed to save configuration:', error)
      throw error
    }
  },

  async getOtherGateways(): Promise<any[]> {
    try {
      const response = await api.get('/discovery')
      return response.data
    } catch (error) {
      console.error('Failed to fetch other gateways:', error)
      return []
    }
  },

  async getStatus(): Promise<GatewayStatus | null> {
    try {
      const response = await api.get('/status')
      return response.data
    } catch (error) {
      console.error('Failed to fetch status:', error)
      return null
    }
  },
}
