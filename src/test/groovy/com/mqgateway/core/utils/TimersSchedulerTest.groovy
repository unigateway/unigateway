package com.mqgateway.core.utils

import java.time.LocalDateTime
import org.jetbrains.annotations.NotNull
import spock.lang.Specification
import spock.lang.Subject

class TimersSchedulerTest extends Specification {

	@Subject
	TimersScheduler scheduler = new TimersScheduler()

	def "should call all registered timers when check timers is called"() {
		given:
		List<SomeSchedulableTimer> timers = (1..4).collect {new SomeSchedulableTimer() }
		timers.forEach {
			scheduler.registerTimer(it)
		}

		when:
		scheduler.checkTimers()

		then:
		timers.every {it.hasBeenCalled }
	}

	def "should not call timer which has been unregistered"() {
		given:
		List<SomeSchedulableTimer> timers = (1..4).collect {new SomeSchedulableTimer() }
		timers.forEach {
			scheduler.registerTimer(it)
		}
		def unregisteredTimer = timers[2]
		scheduler.unregisterTimer(unregisteredTimer)

		when:
		scheduler.checkTimers()

		then:
		!unregisteredTimer.hasBeenCalled
	}
}

class SomeSchedulableTimer implements TimersScheduler.SchedulableTimer {

	boolean hasBeenCalled = false

	@Override
	void updateTimer(@NotNull LocalDateTime dateTime) {
		hasBeenCalled = true
	}
}
