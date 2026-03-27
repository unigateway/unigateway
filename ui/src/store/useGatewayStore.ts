import { create } from 'zustand'

export interface Device {
  id: string
  name: string
  type: string
  properties: Map<string, any>
  isModifiedOrNewDevice?: boolean
}

export interface GatewayConfiguration {
  configVersion: string
  id: string
  name: string
  devices: Device[]
  isModified?: boolean
}

export type ConnectionState = 'CONNECTED' | 'DISCONNECTED' | 'CONNECTING'

interface GatewayState {
  // Configuration
  configuration: GatewayConfiguration | null
  yamlConfiguration: string

  // Connection
  connectionState: ConnectionState

  // Actions
  setConfiguration: (config: GatewayConfiguration) => void
  setYamlConfiguration: (yaml: string) => void
  setConnectionState: (state: ConnectionState) => void
  updateDeviceProperty: (deviceId: string, propertyId: string, value: any) => void
  findDevice: (deviceId: string) => Device | undefined
}

export const useGatewayStore = create<GatewayState>((set, get) => ({
  configuration: null,
  yamlConfiguration: '',
  connectionState: 'DISCONNECTED',

  setConfiguration: (config) => set({ configuration: config }),

  setYamlConfiguration: (yaml) => set({ yamlConfiguration: yaml }),

  setConnectionState: (state) => set({ connectionState: state }),

  updateDeviceProperty: (deviceId, propertyId, value) => {
    const config = get().configuration
    if (!config) return

    const device = config.devices.find(d => d.id === deviceId)
    if (device) {
      device.properties.set(propertyId, value)
      set({ configuration: { ...config } })
    }
  },

  findDevice: (deviceId) => {
    const config = get().configuration
    return config?.devices.find(d => d.id === deviceId)
  },
}))
