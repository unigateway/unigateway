package com.mqgateway.discovery

import com.mqgateway.utils.MqttSpecification
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import javax.jmdns.JmDNS

@MicronautTest
class MulticastDnsServiceDiscoveryControllerTest extends MqttSpecification {

  @Inject
  JmDNS jmDNS
  JmDnsStub jmDnsStub = new JmDnsStub(InetAddress.getByName("192.168.6.1"))

  @Inject
  @Client("/")
  HttpClient client

  @Inject
  MulticastDnsServiceDiscovery serviceDiscovery

  def "should receive a list of other MqGateways in the local network"() {
    given:
    jmDNS.addServiceListener(_, _) >> { type, listener -> jmDnsStub.addServiceListener(type, listener) }
    serviceDiscovery.onApplicationEvent(new ServerStartupEvent(Mock(EmbeddedServer)))
    jmDnsStub.handleServiceResolved("_mqgateway._tcp.local.", "gateway1", 7777, "192.168.6.101")
    jmDnsStub.handleServiceResolved("_mqgateway._tcp.local.", "gateway2", 8888, "192.168.6.102")
    jmDnsStub.handleServiceResolved("_mqgateway._tcp.local.", "gateway3", 9999, "192.168.6.103")

    when:
    HttpRequest request = HttpRequest.GET("/discovery")
    def response = client.toBlocking().exchange(request, List<Map>)

    then:
    response.status == HttpStatus.OK
    response.body().toSet() == [
      [name: "gateway1", ipAddress: "192.168.6.101", portNumber: 7777],
      [name: "gateway2", ipAddress: "192.168.6.102", portNumber: 8888],
      [name: "gateway3", ipAddress: "192.168.6.103", portNumber: 9999]
    ].toSet()
  }

  @MockBean(JmDNS)
  JmDNS jmDNS() {
    return Mock(JmDNS)
  }
}
