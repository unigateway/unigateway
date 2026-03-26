import { useState, useEffect, useRef } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { ScrollArea } from '@/components/ui/scroll-area'
import { FileText } from 'lucide-react'
import { gatewayLogsWebSocket, type Log } from '@/lib/logsWebsocket'
import dayjs from 'dayjs'

const MAX_LOGS = 500

export default function Logs() {
  const [logs, setLogs] = useState<Log[]>([])
  const scrollAreaRef = useRef<HTMLDivElement>(null)
  const shouldAutoScroll = useRef(true)

  useEffect(() => {
    // Connect to logs WebSocket
    const wsUrl = `ws://${window.location.host}/logs/uiLogViewer`
    gatewayLogsWebSocket.connect(wsUrl)

    // Set up log callback for new logs
    gatewayLogsWebSocket.onLog((log) => {
      setLogs((prevLogs) => {
        const newLogs = [...prevLogs, log]
        // Keep only last MAX_LOGS
        if (newLogs.length > MAX_LOGS) {
          newLogs.shift()
        }
        return newLogs
      })
    })

    // Fetch initial logs
    gatewayLogsWebSocket.startReadingLogs().then((initialLogs) => {
      const limitedLogs = initialLogs.slice(-MAX_LOGS)
      setLogs(limitedLogs)
    })

    return () => {
      gatewayLogsWebSocket.disconnect()
    }
  }, [])

  // Auto-scroll to bottom when new logs arrive
  useEffect(() => {
    if (shouldAutoScroll.current && scrollAreaRef.current) {
      const scrollContainer = scrollAreaRef.current.querySelector('[data-radix-scroll-area-viewport]')
      if (scrollContainer) {
        scrollContainer.scrollTop = scrollContainer.scrollHeight
      }
    }
  }, [logs])

  const getLevelColor = (level: string) => {
    switch (level.toUpperCase()) {
      case 'ERROR':
        return 'text-red-600 dark:text-red-400'
      case 'WARN':
        return 'text-orange-600 dark:text-orange-400'
      case 'INFO':
        return 'text-green-600 dark:text-green-400'
      case 'DEBUG':
        return 'text-blue-600 dark:text-blue-400'
      case 'TRACE':
        return 'text-gray-600 dark:text-gray-400'
      default:
        return 'text-foreground'
    }
  }

  return (
    <div className="p-8 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Logs</h1>
        <p className="text-muted-foreground">
          System and device event logs ({logs.length} entries)
        </p>
      </div>

      {/* Logs Display */}
      <Card>
        <CardHeader>
          <CardTitle>System Logs</CardTitle>
          <CardDescription>Real-time log viewer</CardDescription>
        </CardHeader>
        <CardContent>
          {logs.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <FileText className="h-12 w-12 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold">No logs available</h3>
              <p className="text-sm text-muted-foreground">
                Logs will appear here as events occur
              </p>
            </div>
          ) : (
            <ScrollArea className="h-[600px] w-full rounded-md border" ref={scrollAreaRef}>
              <div className="p-4 font-mono text-sm space-y-1">
                {logs.map((log, index) => (
                  <div
                    key={index}
                    className="flex items-start gap-3 py-1 hover:bg-accent/50 rounded px-2"
                  >
                    <span className="text-muted-foreground whitespace-nowrap">
                      {dayjs(log.time).format('YYYY-MM-DD HH:mm:ss')}
                    </span>
                    <span className={`font-semibold whitespace-nowrap min-w-[60px] ${getLevelColor(log.level)}`}>
                      {log.level.padEnd(5)}
                    </span>
                    <span className="flex-1">{log.message}</span>
                  </div>
                ))}
              </div>
            </ScrollArea>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
