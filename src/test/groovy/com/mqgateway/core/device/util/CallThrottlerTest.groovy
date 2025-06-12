package com.mqgateway.core.device.util


import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.Duration

class CallThrottlerTest extends Specification {

  MutableClock clock = new MutableClock()

  def "should run throttled function instantly when interval is set to 0"() {
    given:
    CallThrottler throttler = new CallThrottler(0)
    throttler.setClockForTests(clock)
    boolean received = false

    when:
    throttler.throttle({ received = true })

    then:
    received
  }

  def "should omit the function call when min interval has not passed"() {
    given:
    long minIntervalMillis = 1000
    CallThrottler throttler = new CallThrottler(minIntervalMillis)
    throttler.setClockForTests(clock)
    boolean received1 = false
    boolean received2 = false
    throttler.throttle({ received1 = true })
    clock.plus(Duration.ofMillis(999))

    when:
    throttler.throttle({ received2 = true })

    then:
    received1
    !received2
  }

  def "should run throttled function when interval has passed"() {
    given:
    long minIntervalMillis = 1000
    CallThrottler throttler = new CallThrottler(minIntervalMillis)
    throttler.setClockForTests(clock)
    boolean received1 = false
    boolean received2 = false
    throttler.throttle({ received1 = true })
    clock.plus(Duration.ofMillis(1001))

    when:
    throttler.throttle({ received2 = true })

    then:
    received1
    received2
  }
}
