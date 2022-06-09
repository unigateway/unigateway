package com.unigateway.core.gatewayconfig

import static kotlin.jvm.JvmClassMappingKt.getKotlinClass

import com.unigateway.core.device.DeviceType
import com.unigateway.core.hardware.mqgateway.MqGatewayConnector
import com.unigateway.core.hardware.mqgateway.WireColor
import com.unigateway.core.hardware.simulated.SimulatedConnector
import com.unigateway.core.io.provider.Connector
import com.unigateway.core.io.provider.MySensorsConnector
import com.unigateway.core.mysensors.InternalType
import com.unigateway.core.mysensors.PresentationType
import com.unigateway.core.mysensors.SetReqType
import com.unigateway.core.mysensors.StreamType
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
      new DeviceConfiguration("device_3", "Third device", DeviceType.RELAY, ["state": new MySensorsConnector(1, 1, PresentationType.S_TEMP)]),
      new DeviceConfiguration("device_4", "Fourth device", DeviceType.RELAY, ["state": new MySensorsConnector(1, 1, StreamType.STREAM)]),
      new DeviceConfiguration("device_5", "Fifth device", DeviceType.RELAY, ["state": new MySensorsConnector(1, 1, InternalType.I_BATTERY_LEVEL)]),
      new DeviceConfiguration("device_6", "Sixth device", DeviceType.RELAY, ["state": new MySensorsConnector(1, 1, SetReqType.V_ARMED)]),
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
