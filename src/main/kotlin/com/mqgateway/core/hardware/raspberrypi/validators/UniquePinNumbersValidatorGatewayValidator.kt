package com.mqgateway.core.hardware.raspberrypi.validators

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import com.mqgateway.core.hardware.raspberrypi.RaspberryPiConnector
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class UniquePinNumbersValidatorGatewayValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    LOGGER.debug { "Validating that pin numbers are unique in all devices" }

    return gatewayConfiguration.devices
      .flatMap {
        it.connectors
          .filter { (_, connector) -> connector is RaspberryPiConnector }
          .map { (connectorName, connector) -> Triple(it.id, connectorName, (connector as RaspberryPiConnector).pin) }
      }
      .groupBy { (_, _, pin) -> pin }
      .filter { it.value.size > 1 }
      .map { (pin, group) -> PinNumberNotUnique(pin, group.map { (deviceId, connectorName, _) -> Pair(deviceId, connectorName) }) }
  }

  data class PinNumberNotUnique(private val pin: Int, private val connectorsOnDevices: List<Pair<String, String>>) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Configuration contains the same pin number '$pin' on the following connectors: ${connectorsOnDevices
        .map { "<deviceId: ${it.first}, connector: ${it.second}>" }}"
    }
  }
}
