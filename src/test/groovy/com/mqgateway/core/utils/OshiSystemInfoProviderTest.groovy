package com.mqgateway.core.utils

import spock.lang.Specification
import spock.lang.Subject

class OshiSystemInfoProviderTest extends Specification {

  @Subject
  OshiSystemInfoProvider provider = new OshiSystemInfoProvider()

  def "should return system info"() {
    expect:
    !provider.getIPAddresses().isEmpty()
    provider.getMemoryFree() != 0
    provider.getUptime() != null
  }
}
