import { useState } from 'react'
import { Search, Layers } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Switch } from '@/components/ui/switch'
import { useGatewayStore, type Device } from '@/store/useGatewayStore'
import { gatewayWebSocket } from '@/lib/websocket'

export default function Devices() {
  const [searchQuery, setSearchQuery] = useState('')
  const [view, setView] = useState<'action' | 'config'>('action')

  const configuration = useGatewayStore(state => state.configuration)
  const updateDeviceProperty = useGatewayStore(state => state.updateDeviceProperty)
  const devices = configuration?.devices || []

  const filteredDevices = devices.filter((device) =>
    device.name.toLowerCase().includes(searchQuery.toLowerCase())
  )

  const handleDeviceStateChange = async (deviceId: string, propertyId: string, newValue: any) => {
    try {
      // Optimistically update UI
      updateDeviceProperty(deviceId, propertyId, newValue)
      // Send to backend
      await gatewayWebSocket.updateState(deviceId, propertyId, newValue)
      console.log(`Successfully updated ${deviceId}.${propertyId} to ${newValue}`)
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Unknown error'
      console.error('Failed to update device state:', errorMessage, error)
      // TODO: Could revert the optimistic update here if needed
      // For now, the optimistic update stays even if backend fails
    }
  }

  const renderDeviceAction = (device: Device) => {
    const state = device.properties.get('state')

    switch (device.type) {
      case 'RELAY':
      case 'EMULATED_SWITCH':
        return (
          <div className="flex flex-col items-center gap-3">
            <div className="flex items-center gap-4">
              <span className={`text-sm font-medium ${state === 'OFF' ? 'text-foreground' : 'text-muted-foreground'}`}>
                OFF
              </span>
              <Switch
                checked={state === 'ON'}
                onCheckedChange={(checked) =>
                  handleDeviceStateChange(device.id, 'state', checked ? 'ON' : 'OFF')
                }
                className="scale-125"
              />
              <span className={`text-sm font-medium ${state === 'ON' ? 'text-foreground' : 'text-muted-foreground'}`}>
                ON
              </span>
            </div>
            <div className="text-xs text-muted-foreground">
              Current: {state || 'Unknown'}
            </div>
          </div>
        )

      case 'SWITCH_BUTTON':
      case 'REED_SWITCH':
      case 'MOTION_DETECTOR':
        return (
          <div className="text-center">
            <div className="text-3xl font-bold">{state || 'N/A'}</div>
            <div className="text-xs text-muted-foreground mt-1">Current State</div>
          </div>
        )

      case 'TEMPERATURE':
        const temp = device.properties.get('temperature')
        return (
          <div className="text-center">
            <div className="text-3xl font-bold">{temp ? `${temp}°C` : 'N/A'}</div>
            <div className="text-xs text-muted-foreground mt-1">Temperature</div>
          </div>
        )

      case 'HUMIDITY':
        const humidity = device.properties.get('humidity')
        return (
          <div className="text-center">
            <div className="text-3xl font-bold">{humidity ? `${humidity}%` : 'N/A'}</div>
            <div className="text-xs text-muted-foreground mt-1">Humidity</div>
          </div>
        )

      case 'SHUTTER':
      case 'GATE':
        return (
          <div className="flex flex-col gap-2 w-full">
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleDeviceStateChange(device.id, 'state', 'OPEN')}
            >
              Open
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleDeviceStateChange(device.id, 'state', 'STOP')}
            >
              Stop
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleDeviceStateChange(device.id, 'state', 'CLOSE')}
            >
              Close
            </Button>
            <div className="text-xs text-center text-muted-foreground mt-1">
              {state || 'Unknown'}
            </div>
          </div>
        )

      default:
        return (
          <div className="text-sm text-muted-foreground">
            Not implemented: {device.type}
          </div>
        )
    }
  }

  return (
    <div className="flex flex-col h-full">
      {/* Header with Search */}
      <div className="border-b bg-card">
        <div className="p-4 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold tracking-tight">Devices</h1>
              <p className="text-sm text-muted-foreground">
                Manage and monitor your connected devices
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setView(view === 'action' ? 'config' : 'action')}
            >
              <Layers className="h-4 w-4 mr-2" />
              {view === 'action' ? 'Show Config' : 'Show Actions'}
            </Button>
          </div>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search devices..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>
      </div>

      {/* Devices Grid */}
      <div className="flex-1 overflow-y-auto p-8">
        {filteredDevices.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-64 text-center">
            <Layers className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold">No devices found</h3>
            <p className="text-sm text-muted-foreground">
              {searchQuery
                ? 'Try adjusting your search query'
                : 'Connect devices to get started'}
            </p>
          </div>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {filteredDevices.map((device) => (
              <Card key={device.id} className="flex flex-col">
                <CardHeader>
                  <CardTitle className="text-base truncate">{device.name}</CardTitle>
                  <CardDescription className="text-xs">{device.type}</CardDescription>
                </CardHeader>
                <CardContent className="flex-1">
                  {view === 'config' ? (
                    <div className="space-y-2 text-sm">
                      <div className="text-muted-foreground">
                        <span className="font-medium">ID:</span> {device.id}
                      </div>
                      <div className="text-muted-foreground">
                        <span className="font-medium">Type:</span> {device.type}
                      </div>
                      {device.properties.size > 0 && (
                        <div className="text-muted-foreground">
                          <span className="font-medium">Properties:</span> {device.properties.size}
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="flex flex-col items-center justify-center h-full min-h-[100px]">
                      {renderDeviceAction(device)}
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
