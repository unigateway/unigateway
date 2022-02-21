package com.mqgateway.configuration

import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.discovery.MulticastDnsServiceDiscovery
import io.micronaut.context.annotation.Factory
import io.micronaut.runtime.server.EmbeddedServer
import java.net.Inet4Address
import javax.inject.Singleton
import javax.jmdns.JmDNS

@Factory
class MdnsDiscoveryFactory {

  @Singleton
  fun jmDns(): JmDNS {
    return JmDNS.create(Inet4Address.getLocalHost())
  }

  @Singleton
  fun multicastDnsServiceDiscovery(
    jmDNS: JmDNS,
    gatewayConfiguration: GatewayConfiguration,
    embeddedServer: EmbeddedServer
  ): MulticastDnsServiceDiscovery {
    return MulticastDnsServiceDiscovery(jmDNS, gatewayConfiguration.name, embeddedServer.port)
  }
}
