package com.mqgateway.core.device.buzzer

import com.mqgateway.core.hardware.simulated.SimulatedBinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.utils.UpdateListenerStub
import java.time.Duration
import spock.lang.Specification
import spock.lang.Subject
import spock.util.time.MutableClock

class BuzzerDeviceTest extends Specification {
  def binaryOutput = new SimulatedBinaryOutput()
  MutableClock clock = new MutableClock()
  TimerFake timer = new TimerFake()

  @Subject
  BuzzerDevice buzzer = new BuzzerDevice("buzzer1", "Buzzer", binaryOutput, BinaryState.LOW, [:])

  void setup() {
    buzzer.setClockForTests(clock)
    buzzer.setTimerForTests(timer)
  }

  def "should start and stop beeping"() {
    when:
    buzzer.change("state", "ON")

    then:
    binaryOutput.getState() == BinaryState.LOW

    when:
    buzzer.change("state", "OFF")

    then:
    binaryOutput.getState() == BinaryState.HIGH
  }

  def "should beep continuously for given number of seconds"() {
    when:
    buzzer.change("mode", "CONTINUOUS")
    buzzer.change("timer", "1")

    then:
    binaryOutput.getState() == BinaryState.LOW
    timer.lastOneShotDelay() == 1000

    when:
    clock.plus(Duration.ofSeconds(1))
    timer.runLastOneShotNow()

    then:
    binaryOutput.getState() == BinaryState.HIGH
  }

  def "should beep with breaks for given number of seconds"() {
    when:
    buzzer.change("mode", "INTERVAL")
    buzzer.change("timer", "1")

    then:
    binaryOutput.getState() == BinaryState.LOW
    timer.lastPeriodicDelay() == 500
    timer.lastPeriodicPeriod() == 500
    timer.lastOneShotDelay() == 1000

    when:
    clock.plus(Duration.ofMillis(500))
    timer.runLastPeriodicNow()

    then:
    binaryOutput.getState() == BinaryState.HIGH

    when:
    clock.plus(Duration.ofMillis(500))
    timer.runLastOneShotNow()

    then:
    binaryOutput.getState() == BinaryState.HIGH
  }

  def "should notify listeners on state and mode updates"() {
    given:
    def listenerStub = new UpdateListenerStub()
    buzzer.addListener(listenerStub)
    buzzer.init()

    when:
    buzzer.change("mode", "INTERVAL")
    buzzer.change("state", "ON")
    buzzer.change("state", "OFF")

    then:
    listenerStub.receivedUpdates.find { it.propertyId == "mode" } ==
      new UpdateListenerStub.Update("buzzer1", "mode", "INTERVAL")
    listenerStub.receivedUpdates.find { it.propertyId == "state" } ==
      new UpdateListenerStub.Update("buzzer1", "state", "ON")
    listenerStub.receivedUpdates.findAll { it.propertyId == "state" }.last() ==
      new UpdateListenerStub.Update("buzzer1", "state", "OFF")
  }
}

class TimerFake extends Timer {
  List<TimerTaskWrapper> oneShotTasks = []
  List<TimerTaskWrapper> periodicTasks = []

  @Override
  void schedule(TimerTask task, long delay) {
    oneShotTasks.add(new TimerTaskWrapper(task, delay))
  }

  @Override
  void schedule(TimerTask task, long delay, long period) {
    periodicTasks.add(new TimerTaskWrapper(task, delay, period))
  }

  long lastOneShotDelay() {
    return oneShotTasks.last().delay
  }

  long lastPeriodicDelay() {
    return periodicTasks.last().delay
  }

  long lastPeriodicPeriod() {
    return periodicTasks.last().period
  }

  void runLastOneShotNow() {
    oneShotTasks.last().task.run()
  }

  void runLastPeriodicNow() {
    periodicTasks.last().task.run()
  }
}

class TimerTaskWrapper {
  TimerTask task
  long delay
  Long period

  TimerTaskWrapper(TimerTask task, long delay, Long period = null) {
    this.task = task
    this.delay = delay
    this.period = period
  }
}
