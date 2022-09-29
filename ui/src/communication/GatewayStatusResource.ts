export default interface GatewayStatusResource {
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

