package com.mqgateway.discovery

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.micronaut.serde.annotation.Serdeable
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

private val LOGGER = KotlinLogging.logger {}

class MulticastDnsServiceDiscovery(private val jmDns: JmDNS, private val gatewayName: String) : ApplicationEventListener<ServerStartupEvent> {
  private val gateways: MutableMap<String, MqGateway> = mutableMapOf()
  private var portNumber: Int? = null

  override fun onApplicationEvent(event: ServerStartupEvent) {
    LOGGER.info { "Starting multi-cast service discovery..." }
    portNumber = event.source.port
    jmDns.registerService(ServiceInfo.create("_mqgateway._tcp.local.", gatewayName, portNumber!!, "path=/ui"))
    jmDns.addServiceListener("_mqgateway._tcp.local.", MultiCaseDnsListener())
    LOGGER.info { "Multi-cast service discovery started" }
  }

  fun getGateways(): List<MqGateway> {
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
        LOGGER.info { "New MqGateway found: $info" }
        gateways[info.name] = MqGateway(info.name, info.inetAddresses[0], info.port)
      }
    }
  }
}

@Serdeable
data class MqGateway(val name: String, val ipAddress: InetAddress, val portNumber: Int)
