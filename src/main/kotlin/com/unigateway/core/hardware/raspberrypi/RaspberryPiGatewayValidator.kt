package com.unigateway.core.hardware.raspberrypi

import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.gatewayconfig.validation.GatewayValidator
import com.unigateway.core.gatewayconfig.validation.ValidationFailureReason
import com.unigateway.core.hardware.raspberrypi.validators.UniqueGpioNumbersValidatorGatewayValidator
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class RaspberryPiGatewayValidator : GatewayValidator {

  private val validators: List<GatewayValidator> = listOf(
    UniqueGpioNumbersValidatorGatewayValidator()
  )

  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    LOGGER.info { "Validating configuration for RaspberryPi" }
    return validators.flatMap { it.validate(gatewayConfiguration) }
  }
}
