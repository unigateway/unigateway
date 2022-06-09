package com.unigateway.webapi

import com.unigateway.configuration.GatewayApplicationProperties
import com.unigateway.core.utils.SystemInfoProvider
import com.unigateway.homie.MqttStatusIndicator
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/status")
open class GatewayStatusController(
  private val systemInfoProvider: SystemInfoProvider,
  private val mqttStatusIndicator: MqttStatusIndicator,
  private val gatewayApplicationProperties: GatewayApplicationProperties,
  private val updateChecker: UpdateChecker
) {

  @Get
  fun getStatus(): GatewayStatusResource {
    return GatewayStatusResource(
      cpuTemperature = systemInfoProvider.getCpuTemperature(),
      freeMemoryBytes = systemInfoProvider.getMemoryFree(),
      uptimeSeconds = systemInfoProvider.getUptime().toMillis() / 1000,
      ipAddress = systemInfoProvider.getIPAddresses(),
      mqttConnected = mqttStatusIndicator.isConnected,
      firmwareVersion = gatewayApplicationProperties.appVersion,
      unigatewayLatestVersion = updateChecker.getLatestVersionInfo()
    )
  }
}

data class GatewayStatusResource(
  val cpuTemperature: Float,
  val freeMemoryBytes: Long,
  val uptimeSeconds: Long,
  val ipAddress: String,
  val mqttConnected: Boolean,
  val firmwareVersion: String,
  val unigatewayLatestVersion: ReleaseInfo
)
