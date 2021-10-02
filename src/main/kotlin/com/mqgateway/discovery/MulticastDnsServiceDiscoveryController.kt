package com.mqgateway.discovery

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/discovery")
class MulticastDnsServiceDiscoveryController(private val multicastDnsServiceDiscovery: MulticastDnsServiceDiscovery) {

  @Get
  fun getMqGateways(): List<MqGateway> {
    return multicastDnsServiceDiscovery.getGateways()
  }
}
