package com.mqgateway.configuration

import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.discovery.MulticastDnsServiceDiscovery
import io.micronaut.context.annotation.Factory
import java.net.Inet4Address
import jakarta.inject.Singleton
import javax.jmdns.JmDNS

@Factory
class MdnsDiscoveryFactory {

  @Singleton
  fun jmDns(): JmDNS {
    return JmDNS.create(Inet4Address.getLocalHost())
  }

  @Singleton
  fun multicastDnsServiceDiscovery(jmDNS: JmDNS, gateway: Gateway): MulticastDnsServiceDiscovery {
    return MulticastDnsServiceDiscovery(jmDNS, gateway.name)
  }
}
