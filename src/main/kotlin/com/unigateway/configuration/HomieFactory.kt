package com.unigateway.configuration

import com.unigateway.core.device.DeviceRegistry
import com.unigateway.homie.HomieDevice
import com.unigateway.homie.MqttStatusIndicator
import com.unigateway.homie.gateway.GatewayHomieReceiver
import com.unigateway.homie.gateway.GatewayHomieUpdateListener
import com.unigateway.homie.gateway.HomieDeviceFactory
import com.unigateway.homie.mqtt.HiveMqttClientFactory
import com.unigateway.homie.mqtt.MqttClientFactory
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class HomieFactory {

  @Singleton
  fun mqttClientFactory(gatewaySystemProperties: GatewaySystemProperties) = HiveMqttClientFactory(gatewaySystemProperties.mqttHostname)

  @Singleton
  fun homieDevice(
    mqttClientFactory: MqttClientFactory,
    homieReceiver: GatewayHomieReceiver,
    mqttConnectionListeners: List<HomieDevice.MqttConnectionListener>,
    gatewayApplicationProperties: GatewayApplicationProperties,
    gatewaySystemProperties: GatewaySystemProperties,
    deviceRegistry: DeviceRegistry
  ): HomieDevice {

    val homieDevice = HomieDeviceFactory(mqttClientFactory, homieReceiver, gatewayApplicationProperties.appVersion)
      .toHomieDevice(deviceRegistry, gatewaySystemProperties.networkAdapter)

    mqttConnectionListeners.forEach { listener ->
      homieDevice.addMqttConnectedListener(listener)
    }

    return homieDevice
  }

  @Singleton
  fun homieReceiver(deviceRegistry: DeviceRegistry) = GatewayHomieReceiver(deviceRegistry)

  @Singleton
  fun gatewayHomieUpdateListener(
    homieDevice: HomieDevice
  ): GatewayHomieUpdateListener {

    return GatewayHomieUpdateListener(homieDevice)
  }

  @Singleton
  fun mqttStatusIndicator(): MqttStatusIndicator {
    return MqttStatusIndicator()
  }
}
