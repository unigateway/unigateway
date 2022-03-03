package com.mqgateway.core.gatewayconfig


import static kotlin.jvm.JvmClassMappingKt.getKotlinClass

import com.mqgateway.core.hardware.mqgateway.MqGatewayConnector
import com.mqgateway.core.hardware.mqgateway.WireColor
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.io.provider.Connector
import com.mqgateway.core.io.provider.MySensorsConnector
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializersKt
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import spock.lang.Specification

class FastConfigurationSerializerTest extends Specification {

  def serializer = new FastConfigurationSerializer(simulatedModule())

  def "should serialize and deserialize configuration"() {
    given:
    def config = new GatewayConfiguration("1.0.0", "unigateway-id", "Test config", [
      new DeviceConfiguration("device_1", "First device", DeviceType.RELAY, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("device_2", "Second device", DeviceType.RELAY, ["state": new SimulatedConnector(2)]),
      new DeviceConfiguration("device_3", "Third device", DeviceType.RELAY, ["state": new MySensorsConnector(1)]),
    ])

    when:
    def bytes = serializer.encode(config)

    and:
    def deserializedConfig = serializer.decode(bytes)

    then:
    config == deserializedConfig
  }

  def "should fail when trying to serialize different connector type"() {
    given:
    def config = new GatewayConfiguration("1.0.0", "unigateway-id", "Test config", [
      new DeviceConfiguration("device_1", "First device", DeviceType.RELAY, ["state": new MqGatewayConnector(1, WireColor.BLUE, 100)])
    ])

    when:
    serializer.encode(config)

    then:
    thrown(SerializationException)
  }

  private SerializersModule simulatedModule() {
    def builder = new SerializersModuleBuilder()

    def connectorKClass = getKotlinClass(Connector.class)
    def simulatedConnectorKClass = getKotlinClass(SimulatedConnector.class)
    def polymorphicModuleBuilder = new PolymorphicModuleBuilder(connectorKClass, null)
    polymorphicModuleBuilder.subclass(simulatedConnectorKClass, SerializersKt.serializer(simulatedConnectorKClass))
    polymorphicModuleBuilder.buildTo(builder)

    return builder.build()
  }
}
