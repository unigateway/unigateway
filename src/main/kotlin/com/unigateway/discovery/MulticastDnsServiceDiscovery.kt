package com.unigateway.discovery

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import mu.KotlinLogging
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

private val LOGGER = KotlinLogging.logger {}

class MulticastDnsServiceDiscovery(private val jmDns: JmDNS, private val gatewayName: String) : ApplicationEventListener<ServerStartupEvent> {

  private val gateways: MutableMap<String, Unigateway> = mutableMapOf()
  private var portNumber: Int? = null

  override fun onApplicationEvent(event: ServerStartupEvent) {
    LOGGER.info { "Starting multi-cast service discovery..." }
    portNumber = event.source.port
    jmDns.registerService(ServiceInfo.create("_unigateway._tcp.local.", gatewayName, portNumber!!, "path=/ui"))
    jmDns.addServiceListener("_unigateway._tcp.local.", MultiCaseDnsListener())
    LOGGER.info { "Multi-cast service discovery started" }
  }

  fun getGateways(): List<Unigateway> {
    return gateways.values.toList()
  }

  private inner class MultiCaseDnsListener : ServiceListener {
    override fun serviceAdded(event: ServiceEvent) {
      LOGGER.debug { "Service added: ${event.info}, but not resolved yet" }
    }

    override fun serviceRemoved(event: ServiceEvent) {
      val info = event.info
      LOGGER.info { "Service removed: $info" }
      gateways.remove(info.name)
    }

    override fun serviceResolved(event: ServiceEvent) {
      val info = event.info
      LOGGER.info { "Service resolved: $info" }
      if (info.inetAddresses[0] != jmDns.inetAddress || info.port != portNumber) {
        LOGGER.info { "New Unigateway found: $info" }
        gateways[info.name] = Unigateway(info.name, info.inetAddresses[0], info.port)
      }
    }
  }
}

data class Unigateway(val name: String, val ipAddress: InetAddress, val portNumber: Int)
