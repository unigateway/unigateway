package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import com.mqgateway.core.hardware.mqgateway.validators.PortNumbersRangeValidator
import com.mqgateway.core.hardware.mqgateway.validators.UniqueWiresValidatorGatewayValidator
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class MqGatewayGatewayValidator(platformConfiguration: MqGatewayPlatformConfiguration) : GatewayValidator {
  private val validators: List<GatewayValidator> =
    listOf(
      UniqueWiresValidatorGatewayValidator(),
      PortNumbersRangeValidator(platformConfiguration),
    )

  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    LOGGER.info { "Validating configuration for MqGateway" }
    return validators.flatMap { it.validate(gatewayConfiguration) }
  }
}
