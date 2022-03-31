package com.mqgateway.core.hardware.raspberrypi.validators

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import com.mqgateway.core.hardware.raspberrypi.RaspberryPiConnector
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class PinNumberRangeValidatorGatewayValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    LOGGER.debug { "Validating pin numbers in range" }

    return gatewayConfiguration.devices
      .flatMap { deviceConfiguration ->
        deviceConfiguration.connectors
          .filter { (_, connector) -> connector is RaspberryPiConnector }
          .filter { (_, connector) -> !isInRange((connector as RaspberryPiConnector).pin) }
          .map { (name, connector) -> PinNumberOutOfRange(deviceConfiguration.id, name, (connector as RaspberryPiConnector).pin) }
      }
  }

  private fun isInRange(pin: Int): Boolean {
    return pin in MIN_PIN_NUMBER..MAX_PIN_NUMBER
  }

  companion object {
    private const val MIN_PIN_NUMBER = 1
    private const val MAX_PIN_NUMBER = 40
  }

  data class PinNumberOutOfRange(private val deviceId: String, private val connectorName: String, private val pin: Int) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Connector '$connectorName' on device '$deviceId' has pin number '$pin' out of range: $MIN_PIN_NUMBER..$MAX_PIN_NUMBER)."
    }
  }
}
