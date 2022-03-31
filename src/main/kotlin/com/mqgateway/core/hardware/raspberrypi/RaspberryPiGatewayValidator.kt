package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import com.mqgateway.core.hardware.raspberrypi.validators.PinNumberRangeValidatorGatewayValidator
import com.mqgateway.core.hardware.raspberrypi.validators.UniquePinNumbersValidatorGatewayValidator
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class RaspberryPiGatewayValidator : GatewayValidator {

  private val validators: List<GatewayValidator> = listOf(
    PinNumberRangeValidatorGatewayValidator(),
    UniquePinNumbersValidatorGatewayValidator()
  )

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    LOGGER.info { "Validating configuration for RaspberryPi" }
    return validators.flatMap { it.validate(gatewayConfiguration, systemProperties) }
  }
}
