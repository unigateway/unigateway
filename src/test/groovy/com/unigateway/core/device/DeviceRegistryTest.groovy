package com.unigateway.core.device

import com.unigateway.utils.DeviceStub
import com.unigateway.utils.TestGatewayFactory
import com.unigateway.utils.UpdateListenerStub
import spock.lang.Shared
import spock.lang.Specification

class DeviceRegistryTest extends Specification {

  @Shared
  TestGatewayFactory gatewayFactory = new TestGatewayFactory()

  def "should initialize all devices"() {
    given:
    def devices = (1..4).collect { new DeviceStub("test_$it") }.toSet()
    DeviceRegistry deviceRegistry = new DeviceRegistry(devices)

    when:
    deviceRegistry.initializeDevices()

    then:
    devices.every { it.initialized }
  }

  def "should add updateListener to each device"() {
    given:
    def devices = (1..4).collect { new DeviceStub("test_$it") }.toSet()
    DeviceRegistry deviceRegistry = new DeviceRegistry(devices)
    def updateListenerStub = new UpdateListenerStub()

    when:
    deviceRegistry.addUpdateListener(updateListenerStub)
    devices.each { it.notify(DevicePropertyType.STATE, "new value") }

    then:
    def expectedUpdates = (1..4).collect { new UpdateListenerStub.Update("test_$it", "state", "new value") }
    updateListenerStub.receivedUpdates.toSet() == expectedUpdates.toSet()
  }

  def "should find devices by type"() {
    given:
    def devices = [
      gatewayFactory.relayDevice("test_1"),
      gatewayFactory.unigatewayDevice("test_2"),
      gatewayFactory.relayDevice("test_3"),
      gatewayFactory.switchButtonDevice("test_4")
    ].toSet()
    DeviceRegistry deviceRegistry = new DeviceRegistry(devices)

    when:
    def filteredDevices = deviceRegistry.filterByType(DeviceType.RELAY)

    then:
    filteredDevices*.id.toSet() == ["test_1", "test_3"].toSet()
  }

  def "should get unigateway device"() {
    given:
    def devices = [
      gatewayFactory.relayDevice("test_1"),
      gatewayFactory.unigatewayDevice("test_2"),
      gatewayFactory.relayDevice("test_3"),
      gatewayFactory.switchButtonDevice("test_4")
    ].toSet()
    DeviceRegistry deviceRegistry = new DeviceRegistry(devices)

    when:
    def unigateway = deviceRegistry.getUniGatewayDevice()

    then:
    unigateway.id == "test_2"
    unigateway.type == DeviceType.UNIGATEWAY
  }

  def "should throw error when there is no/more than one unigateway device"(Set<Device> devices) {
    given:
    DeviceRegistry deviceRegistry = new DeviceRegistry(devices)

    when:
    deviceRegistry.getUniGatewayDevice()

    then:
    thrown(IllegalStateException)

    where:
    devices << [
      [gatewayFactory.unigatewayDevice("test_1"), gatewayFactory.unigatewayDevice("test_2")].toSet(),
      [gatewayFactory.relayDevice("test_1")].toSet()
    ]
  }

}


