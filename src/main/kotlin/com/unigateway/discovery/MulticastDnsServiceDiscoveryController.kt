package com.unigateway.discovery

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/discovery")
class MulticastDnsServiceDiscoveryController(private val multicastDnsServiceDiscovery: MulticastDnsServiceDiscovery) {

  @Get
  fun getUnigateways(): List<Unigateway> {
    return multicastDnsServiceDiscovery.getGateways()
  }
}
