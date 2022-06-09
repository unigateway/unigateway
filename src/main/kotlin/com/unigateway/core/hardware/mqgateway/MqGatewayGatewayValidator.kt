package com.unigateway.core.hardware.mqgateway

import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.gatewayconfig.validation.GatewayValidator
import com.unigateway.core.gatewayconfig.validation.ValidationFailureReason
import com.unigateway.core.hardware.mqgateway.validators.PortNumbersRangeValidator
import com.unigateway.core.hardware.mqgateway.validators.UniqueWiresValidatorGatewayValidator
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class MqGatewayGatewayValidator(platformConfiguration: MqGatewayPlatformConfiguration) : GatewayValidator {

  private val validators: List<GatewayValidator> = listOf(
    UniqueWiresValidatorGatewayValidator(),
    PortNumbersRangeValidator(platformConfiguration)
  )

  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    LOGGER.info { "Validating configuration for MqGateway" }
    return validators.flatMap { it.validate(gatewayConfiguration) }
  }
}
