package com.mqgateway.core.hardware.raspberrypi.validators

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import com.mqgateway.core.hardware.raspberrypi.RaspberryPiConnector
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class UniqueGpioNumbersValidatorGatewayValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    LOGGER.debug { "Validating that gpio numbers are unique in all devices" }

    return gatewayConfiguration.devices
      .flatMap {
        it.connectors
          .filter { (_, connector) -> connector is RaspberryPiConnector }
          .map { (connectorName, connector) -> Triple(it.id, connectorName, (connector as RaspberryPiConnector).gpio) }
      }
      .groupBy { (_, _, gpio) -> gpio }
      .filter { it.value.size > 1 }
      .map { (gpio, group) -> GpioNumberNotUnique(gpio, group.map { (deviceId, connectorName, _) -> Pair(deviceId, connectorName) }) }
  }

  data class GpioNumberNotUnique(private val gpio: Int, private val connectorsOnDevices: List<Pair<String, String>>) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Configuration contains the same gpio number '$gpio' on the following connectors: ${connectorsOnDevices
        .map { "<deviceId: ${it.first}, connector: ${it.second}>" }}"
    }
  }
}
