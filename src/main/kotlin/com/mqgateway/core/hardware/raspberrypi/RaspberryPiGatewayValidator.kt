package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import com.mqgateway.core.hardware.raspberrypi.validators.UniqueGpioNumbersValidatorGatewayValidator
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class RaspberryPiGatewayValidator : GatewayValidator {
  private val validators: List<GatewayValidator> =
    listOf(
      UniqueGpioNumbersValidatorGatewayValidator(),
    )

  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    LOGGER.info { "Validating configuration for RaspberryPi" }
    return validators.flatMap { it.validate(gatewayConfiguration) }
  }
}
