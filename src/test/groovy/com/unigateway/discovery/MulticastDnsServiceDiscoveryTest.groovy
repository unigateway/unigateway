package com.unigateway.discovery


import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.runtime.server.event.ServerStartupEvent
import spock.lang.Specification
import spock.lang.Subject

class MulticastDnsServiceDiscoveryTest extends Specification {

  public static final String THIS_GATEWAY_IP_ADDRESS = "192.168.5.1"
  public static final int THIS_GATEWAY_PORT_NUMBER = 9999
  JmDnsStub jmDnsStub = new JmDnsStub(InetAddress.getByName(THIS_GATEWAY_IP_ADDRESS))

  @Subject
  MulticastDnsServiceDiscovery serviceDiscovery = new MulticastDnsServiceDiscovery(jmDnsStub, "testGateway")
  EmbeddedServer embeddedServerMock = Mock(EmbeddedServer)

  void setup() {
    embeddedServerMock.getPort() >> THIS_GATEWAY_PORT_NUMBER
    serviceDiscovery.onApplicationEvent(new ServerStartupEvent(embeddedServerMock))
  }

  def "should add gateway to the list when it is discovered and resolved"() {
    given:
    def ipAddress = "192.168.5.100"
    def portNumber = 8081
    def otherGatewayName = "someTestGw2"

    when:
    jmDnsStub.handleServiceResolved("_unigateway._tcp.local.", otherGatewayName, portNumber, ipAddress)

    then:
    serviceDiscovery.gateways == [new Unigateway(otherGatewayName, Inet4Address.getByName(ipAddress), portNumber)]
  }

  def "should remove gateway from the list when it is removed"() {
    given:
    def ipAddress = "192.168.5.100"
    def portNumber = 8081
    def otherGatewayName = "someTestGw2"
    jmDnsStub.handleServiceResolved("_unigateway._tcp.local.", otherGatewayName, portNumber, ipAddress)

    when:
    jmDnsStub.handleServiceRemoved("_unigateway._tcp.local.", otherGatewayName, portNumber, ipAddress)

    then:
    serviceDiscovery.gateways == []
  }

  def "should not add itself to the list of gateways"() {
    when:
    jmDnsStub.handleServiceResolved("_unigateway._tcp.local.", "thisGateway", THIS_GATEWAY_PORT_NUMBER, THIS_GATEWAY_IP_ADDRESS)

    then:
    serviceDiscovery.gateways == []
  }
}
