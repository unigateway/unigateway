package com.mqgateway.core.hardware.mqgateway.mcp

import static com.mqgateway.core.hardware.mqgateway.mcp.MqGatewayMcpExpander.GPIOA
import static com.mqgateway.core.hardware.mqgateway.mcp.MqGatewayMcpExpander.GPIOB
import static com.mqgateway.core.io.BinaryState.HIGH
import static com.mqgateway.core.io.BinaryState.LOW

import com.diozero.devices.MCP23017
import com.diozero.sbc.BoardPinInfo
import com.mqgateway.core.io.BinaryState
import java.time.Duration
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable
import spock.util.concurrent.PollingConditions
import spock.util.time.MutableClock

class MqGatewayMcpExpanderTest extends Specification {

  static def boardPinInfo = new BoardPinInfo()

  MutableClock clock = new MutableClock()
  MCP23017 mcpMock = Mock(MCP23017)
	MqGatewayMcpExpander expander = new MqGatewayMcpExpander(mcpMock, clock, Mock(ThreadSleeper))
  PollingConditions conditions = new PollingConditions(timeout: 5)

  def setupSpec() {
    (0..15).each {boardPinInfo.addGpioPinInfo(it, it, [])}
  }

  void setup() {
    mcpMock.getBoardPinInfo() >> boardPinInfo
  }

  void cleanup() {
    expander.stop()
  }

  def "should set pin state based on MCP23017 expander"(byte gpioAValues, byte gpioBValues, List<BinaryState> expectedState) {
    given:
    mcpMock.getValues(GPIOA) >> { clock.plus(Duration.ofMillis(10)); return gpioAValues }
    mcpMock.getValues(GPIOB) >> { clock.plus(Duration.ofMillis(10)); return gpioBValues }
    (0..15).each { expander.getInputPin(it, 0) }
    expander.start()

    expect:
    conditions.eventually {
      def expanderState = (0..15).collect { expander.getPinState(it) }
      assert expanderState == expectedState
    }

    where:
    gpioAValues | gpioBValues | expectedState
    0b1111_1111 | 0b1111_1111 | [HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH, HIGH]
    0b0000_0000 | 0b0000_0000 | [LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW, LOW]
    0b1010_0101 | 0b0101_1010 | [HIGH, LOW, HIGH, LOW, LOW, HIGH, LOW, HIGH, LOW, HIGH, LOW, HIGH, HIGH, LOW, HIGH, LOW]
    0b1000_0000 | 0b0000_0001 | [LOW, LOW, LOW, LOW, LOW, LOW, LOW, HIGH, HIGH, LOW, LOW, LOW, LOW, LOW, LOW, LOW]
  }

  def "should notify listener on pin value changed"(int pin, byte gpioAValues, byte gpioBValues, BinaryState expectedState, byte startGpioAValues, byte startGpioBValues) {
    given:
    long startMillis = clock.millis()
    TreeMap<Long, Byte> valuesGpioAInTime = [
      (startMillis): startGpioAValues as Byte,
      (startMillis + 100): gpioAValues as Byte
    ]

    mcpMock.getValues(GPIOA) >> {
      def newValue = getValuesInTime(valuesGpioAInTime, clock.millis())
      return newValue
    }
    TreeMap<Long, Byte> valuesGpioBInTime = [
      (startMillis): startGpioBValues as Byte,
      (startMillis + 100): gpioBValues as Byte
    ]

    mcpMock.getValues(GPIOB) >> {
      def newValue = getValuesInTime(valuesGpioBInTime, clock.millis())
      clock.plus(Duration.ofMillis(1))
      return newValue
    }

    when:
    BlockingVariable<BinaryState> readState = new BlockingVariable<>(5)
    expander.addListener(pin, 0) { event -> readState.set(event.newState()) }
    expander.start()

    then:
    conditions.eventually {
      assert readState.get() == expectedState
    }

    where:
    pin | startGpioAValues | startGpioBValues | gpioAValues | gpioBValues | expectedState
    0   | 0b1111_1111      | 0b1111_1111      | 0b1111_1110 | 0b1111_1111 | LOW
    1   | 0b1111_1111      | 0b1111_1111      | 0b1111_1101 | 0b1111_1111 | LOW
    2   | 0b1111_1111      | 0b1111_1111      | 0b1111_1011 | 0b1111_1111 | LOW
    3   | 0b1111_1111      | 0b1111_1111      | 0b1111_0111 | 0b1111_1111 | LOW
    4   | 0b1111_1111      | 0b1111_1111      | 0b1110_1111 | 0b1111_1111 | LOW
    5   | 0b1111_1111      | 0b1111_1111      | 0b1101_1111 | 0b1111_1111 | LOW
    6   | 0b1111_1111      | 0b1111_1111      | 0b1011_1111 | 0b1111_1111 | LOW
    7   | 0b1111_1111      | 0b1111_1111      | 0b0111_1111 | 0b1111_1111 | LOW
    8   | 0b1111_1111      | 0b1111_1111      | 0b1111_1111 | 0b1111_1110 | LOW
    9   | 0b1111_1111      | 0b1111_1111      | 0b1111_1111 | 0b1111_1101 | LOW
    10  | 0b1111_1111      | 0b1111_1111      | 0b1111_1111 | 0b1111_1011 | LOW
    11  | 0b1111_1111      | 0b1111_1111      | 0b1111_1111 | 0b1111_0111 | LOW
    12  | 0b1111_1111      | 0b1111_1111      | 0b1111_1111 | 0b1110_1111 | LOW
    13  | 0b1111_1111      | 0b1111_1111      | 0b1111_1111 | 0b1101_1111 | LOW
    14  | 0b1111_1111      | 0b1111_1111      | 0b1111_1111 | 0b1011_1111 | LOW
    15  | 0b1111_1111      | 0b1111_1111      | 0b1111_1111 | 0b0111_1111 | LOW
  }

  def "should notify listener on pin state change when change took longer than debounce"() {
    given:
    long startMillis = clock.millis()
    TreeMap<Long, Byte> valuesInTime = [
      (startMillis): 0b1111_1111 as Byte,
      (startMillis + 100): 0b1111_1110 as Byte,
      (startMillis + 201): 0b1111_1111 as Byte
    ]

    mcpMock.getValues(GPIOA) >> {
      def newValue = getValuesInTime(valuesInTime, clock.millis())
      clock.plus(Duration.ofMillis(1))
      return newValue
    }
    mcpMock.getValues(GPIOB) >> 0b1111_1111
    int debounceMs = 100
    BlockingVariable<BinaryState> readState = new BlockingVariable<>(5)

    when:
    expander.addListener(0, debounceMs) { event -> readState.set(event.newState()) }
    expander.start()

    then:
    readState.get() == LOW
  }

  def "should not notify listener on pin state change when change took shorter than debounce"() {
    given:
    long startMillis = clock.millis()
    TreeMap<Long, Byte> valuesInTime = [
      (startMillis): 0b1111_1111 as Byte,
      (startMillis + 100): 0b1111_1110 as Byte,
      (startMillis + 200): 0b1111_1101 as Byte
    ]

    mcpMock.getValues(GPIOA) >> {
      def newValue = getValuesInTime(valuesInTime, clock.millis())
      clock.plus(Duration.ofMillis(10))
      return newValue
    }

    mcpMock.getValues(GPIOB) >> 0b1111_1111
    int debounceMs = 200
    boolean hasReceivedEvent = false

    when:
    expander.addListener(0, debounceMs) { event -> hasReceivedEvent = true }
    BlockingVariable<BinaryState> readState = new BlockingVariable<>(5)
    expander.addListener(1, debounceMs) { event -> readState.set(event.newState()) }
    expander.start()

    then:
    readState.get() == LOW
    !hasReceivedEvent
  }

  def "should fail with exception when trying to use the same pin as input and output"() {
    when:
    expander.getInputPin(1, 50)
    expander.getOutputPin(1)

    then:
    def exception = thrown(McpExpanderPinAlreadyInUseException)
    exception.gpioNumber == 1
    exception.gpioType == GpioType.INPUT
  }

  def "should fail with exception when trying to get the same pin twice"() {
    when:
    expander.getOutputPin(3)
    expander.getOutputPin(3)

    then:
    def exception = thrown(McpExpanderPinAlreadyInUseException)
    exception.gpioNumber == 3
    exception.gpioType == GpioType.OUTPUT
  }

  def "should fail with exception when trying to read state from pin set to be OUTPUT"() {
    given:
    expander.getOutputPin(1)

    when:
    expander.getPinState(1)

    then:
    thrown(IncorrectGpioTypeException)
  }

  def "should fail with exception when trying to write state from pin set to be INPUT"() {
    given:
    expander.getInputPin(1, 50)

    when:
    expander.setPinState(1, HIGH)

    then:
    thrown(IncorrectGpioTypeException)
  }

  private byte getValuesInTime(SortedMap<Long, Byte> valuesByTime, Long currentTimeInMillis) {
    Long timeKeyForCurrentTime
    for (Long timeInMap : valuesByTime.keySet()) {
      if (currentTimeInMillis < timeInMap) {
        break
      }
      timeKeyForCurrentTime = timeInMap
    }
    if (timeKeyForCurrentTime == null) {
      throw new IllegalStateException("Wrongly configured valuesByTime: $valuesByTime")
    }

    return valuesByTime.get(timeKeyForCurrentTime)
  }
}
