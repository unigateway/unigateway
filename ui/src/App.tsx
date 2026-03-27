import { useEffect } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster, toast } from 'sonner'
import AppLayout from './components/layout/AppLayout'
import Dashboard from './pages/Dashboard'
import Devices from './pages/Devices'
import Logs from './pages/Logs'
import { gatewayWebSocket } from './lib/websocket'
import { gatewayApi } from './lib/api'
import { useGatewayStore } from './store/useGatewayStore'

const queryClient = new QueryClient()

function AppContent() {
  const setConfiguration = useGatewayStore(state => state.setConfiguration)
  const setYamlConfiguration = useGatewayStore(state => state.setYamlConfiguration)
  const connectionState = useGatewayStore(state => state.connectionState)

  useEffect(() => {
    // Initialize WebSocket connection
    const wsUrl = `ws://${window.location.host}/devices/ui`
    gatewayWebSocket.connect(wsUrl)

    // Fetch initial configuration and state
    const initialize = async () => {
      try {
        // Fetch configuration first
        const { yaml, config } = await gatewayApi.getConfiguration()
        setYamlConfiguration(yaml)
        setConfiguration(config)

        // Then fetch initial device states from WebSocket
        await gatewayWebSocket.fetchInitialState()
        console.log('Initial state loaded')
      } catch (error) {
        toast.error('Failed to load configuration')
        console.error(error)
      }
    }

    initialize()

    return () => {
      gatewayWebSocket.disconnect()
    }
  }, [setConfiguration, setYamlConfiguration])

  // Show connection status notifications
  useEffect(() => {
    if (connectionState === 'CONNECTED') {
      toast.success('Connected to gateway')
    } else if (connectionState === 'DISCONNECTED') {
      toast.error('Disconnected from gateway')
    }
  }, [connectionState])

  return (
    <AppLayout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/devices" element={<Devices />} />
        <Route path="/logs" element={<Logs />} />
      </Routes>
    </AppLayout>
  )
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter basename="/ui">
        <AppContent />
        <Toaster position="bottom-right" />
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
