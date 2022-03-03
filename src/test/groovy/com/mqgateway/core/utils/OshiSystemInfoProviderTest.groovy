package com.mqgateway.core.utils

import spock.lang.Specification
import spock.lang.Subject

class OshiSystemInfoProviderTest extends Specification {

  @Subject
  OshiSystemInfoProvider provider = new OshiSystemInfoProvider()

  def "should return system info"() {
    expect:
    println("1" + provider.getCpuTemperature())
    println("2" + provider.getIPAddresses())
    println("3" + provider.getMemoryFree())
    println("4" + provider.getUptime())
  }
}
