package com.mqgateway.configuration

import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.discovery.MulticastDnsServiceDiscovery
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import java.net.Inet4Address
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
  ): MulticastDnsServiceDiscovery {
    return MulticastDnsServiceDiscovery(jmDNS, gatewayConfiguration.name)
  }
}
