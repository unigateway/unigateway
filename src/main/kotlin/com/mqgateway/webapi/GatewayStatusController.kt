package com.mqgateway.webapi

import com.mqgateway.configuration.GatewayApplicationProperties
import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.homie.MqttStatusIndicator
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/status")
open class GatewayStatusController(
  private val systemInfoProvider: SystemInfoProvider,
  private val mqttStatusIndicator: MqttStatusIndicator?,
  private val gatewayApplicationProperties: GatewayApplicationProperties,
  private val gatewaySystemProperties: GatewaySystemProperties,
  private val updateChecker: UpdateChecker
) {

  @Get
  fun getStatus(): GatewayStatusResource {
    return GatewayStatusResource(
      cpuTemperature = systemInfoProvider.getCpuTemperature(),
      freeMemoryBytes = systemInfoProvider.getMemoryFree(),
      uptimeSeconds = systemInfoProvider.getUptime().toMillis() / 1000,
      ipAddress = systemInfoProvider.getIPAddresses(),
      mqttConnected = mqttStatusIndicator?.isConnected ?: false,
      mySensorsEnabled = gatewaySystemProperties.mySensors.enabled,
      firmwareVersion = gatewayApplicationProperties.appVersion,
      mqGatewayLatestVersion = updateChecker.getLatestVersionInfo()
    )
  }
}

data class GatewayStatusResource(
  val cpuTemperature: Float,
  val freeMemoryBytes: Long,
  val uptimeSeconds: Long,
  val ipAddress: String,
  val mqttConnected: Boolean,
  val mySensorsEnabled: Boolean,
  val firmwareVersion: String,
  val mqGatewayLatestVersion: ReleaseInfo?
)
