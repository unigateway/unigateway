import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Activity, Server, Clock, ArrowRight, Thermometer, HardDrive, Wifi } from 'lucide-react'
import { useGatewayStore } from '@/store/useGatewayStore'
import { gatewayLogsWebSocket, type Log } from '@/lib/logsWebsocket'
import { gatewayApi, type GatewayStatus } from '@/lib/api'
import dayjs from 'dayjs'

const MAX_RECENT_LOGS = 8

export default function Dashboard() {
  const configuration = useGatewayStore(state => state.configuration)
  const connectionState = useGatewayStore(state => state.connectionState)
  const [recentLogs, setRecentLogs] = useState<Log[]>([])
  const [status, setStatus] = useState<GatewayStatus | null>(null)
  const [newVersionAvailable, setNewVersionAvailable] = useState<string | null>(null)

  const deviceCount = configuration?.devices.length || 0
  const isConnected = connectionState === 'CONNECTED'

  useEffect(() => {
    // Connect to logs WebSocket
    const wsUrl = `ws://${window.location.host}/logs/uiLogViewer`
    gatewayLogsWebSocket.connect(wsUrl)

    // Set up log callback for new logs
    gatewayLogsWebSocket.onLog((log) => {
      setRecentLogs((prevLogs) => {
        const newLogs = [...prevLogs, log]
        // Keep only last MAX_RECENT_LOGS
        return newLogs.slice(-MAX_RECENT_LOGS)
      })
    })

    // Fetch initial logs
    gatewayLogsWebSocket.startReadingLogs().then((initialLogs) => {
      const limitedLogs = initialLogs.slice(-MAX_RECENT_LOGS)
      setRecentLogs(limitedLogs)
    })

    return () => {
      gatewayLogsWebSocket.disconnect()
    }
  }, [])

  useEffect(() => {
    const fetchStatus = async () => {
      const statusData = await gatewayApi.getStatus()
      if (statusData) {
        setStatus(statusData)

        // Check for upgrade availability
        if (statusData.firmwareVersion && statusData.mqGatewayLatestVersion?.tag_name) {
          const cleanedVersion = statusData.firmwareVersion.split('-')[0]
          if (`v${cleanedVersion}` !== statusData.mqGatewayLatestVersion.tag_name) {
            setNewVersionAvailable(statusData.mqGatewayLatestVersion.tag_name)
          } else {
            setNewVersionAvailable(null)
          }
        }
      }
    }

    fetchStatus()
    const interval = setInterval(fetchStatus, 10000)
    return () => clearInterval(interval)
  }, [])

  const getLevelColor = (level: string) => {
    switch (level.toUpperCase()) {
      case 'ERROR':
        return 'text-red-600 dark:text-red-400'
      case 'WARN':
        return 'text-orange-600 dark:text-orange-400'
      case 'INFO':
        return 'text-green-600 dark:text-green-400'
      default:
        return 'text-muted-foreground'
    }
  }

  return (
    <div className="p-8 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">
          {configuration?.name || 'Overview of your gateway status and activity'}
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Gateway Status */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Gateway Status</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-2">
              <Badge
                variant={isConnected ? "default" : "secondary"}
                className={isConnected ? "bg-green-500" : ""}
              >
                {isConnected ? 'Online' : 'Offline'}
              </Badge>
            </div>
            <p className="text-xs text-muted-foreground mt-2">
              {isConnected ? 'Connected and operational' : 'Disconnected from gateway'}
            </p>
          </CardContent>
        </Card>

        {/* Devices Count */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Devices</CardTitle>
            <Server className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{deviceCount}</div>
            <p className="text-xs text-muted-foreground">
              Connected devices
            </p>
          </CardContent>
        </Card>

        {/* Gateway Info */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Gateway ID</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-sm font-mono">{configuration?.id || '--'}</div>
            <p className="text-xs text-muted-foreground">
              Version: {configuration?.configVersion || '--'}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Detailed Status Grid */}
      <Card>
        <CardHeader>
          <CardTitle>System Status</CardTitle>
          <CardDescription>Detailed gateway information</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <Thermometer className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">CPU Temperature</p>
                  <p className="text-sm text-muted-foreground">
                    {status?.cpuTemperature ? `${status.cpuTemperature}°C` : '--'}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Clock className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">Uptime</p>
                  <p className="text-sm text-muted-foreground">
                    {status?.uptimeSeconds ? `${status.uptimeSeconds} seconds` : '--'}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Wifi className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">IP Address</p>
                  <p className="text-sm text-muted-foreground font-mono">
                    {status?.ipAddress || '--'}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <HardDrive className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">Free Memory</p>
                  <p className="text-sm text-muted-foreground">
                    {status?.freeMemoryBytes ? `${Math.trunc(status.freeMemoryBytes / 1000000)}MB` : '--'}
                  </p>
                </div>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <Activity className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">MQTT Status</p>
                  <Badge variant={status?.mqttConnected ? "default" : "secondary"} className={status?.mqttConnected ? "bg-green-500" : ""}>
                    {status?.mqttConnected ? 'Connected' : 'Disconnected'}
                  </Badge>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Server className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">MySensors Status</p>
                  <Badge variant={status?.mySensorsEnabled ? "default" : "secondary"}>
                    {status?.mySensorsEnabled ? 'Enabled' : 'Disabled'}
                  </Badge>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Activity className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">Firmware Version</p>
                  <p className="text-sm text-muted-foreground font-mono">
                    {status?.firmwareVersion || '--'}
                  </p>
                </div>
              </div>

              {newVersionAvailable && status?.mqGatewayLatestVersion && (
                <div className="mt-2">
                  <a
                    href={status.mqGatewayLatestVersion.html_url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm font-semibold text-primary hover:underline"
                  >
                    Upgrade to {newVersionAvailable} available
                  </a>
                </div>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Recent Activity */}
      <div className="grid gap-4 md:grid-cols-2">
        {/* Other Gateways */}
        <Card>
          <CardHeader>
            <CardTitle>Other Gateways</CardTitle>
            <CardDescription>Discovered gateways in your network</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="text-sm text-muted-foreground">
              No other gateways discovered
            </div>
          </CardContent>
        </Card>

        {/* Recent Logs */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Logs</CardTitle>
            <CardDescription>Latest system events</CardDescription>
          </CardHeader>
          <CardContent>
            {recentLogs.length === 0 ? (
              <div className="text-sm text-muted-foreground">
                No recent logs
              </div>
            ) : (
              <div className="space-y-2 max-h-[220px] overflow-y-auto">
                {recentLogs.map((log, index) => (
                  <div key={index} className="text-xs font-mono flex items-start gap-2 py-1">
                    <span className="text-muted-foreground whitespace-nowrap">
                      {dayjs(log.time).format('HH:mm:ss')}
                    </span>
                    <span className={`font-semibold whitespace-nowrap min-w-[45px] ${getLevelColor(log.level)}`}>
                      {log.level}
                    </span>
                    <span className="flex-1 truncate" title={log.message}>
                      {log.message}
                    </span>
                  </div>
                ))}
              </div>
            )}
            {recentLogs.length > 0 && (
              <Link
                to="/logs"
                className="inline-flex items-center gap-1 text-sm text-primary hover:underline mt-4"
              >
                See more logs
                <ArrowRight className="h-3 w-3" />
              </Link>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
