package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.Point
import jakarta.inject.Singleton

@Singleton
class UniquePortNumbersForPointsValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    // TODO Need to move to be implemented with MqGateway Hardware Interface
    return emptyList()
  }

  class DuplicatedPortNumbersOnPoints(val points: List<Point>) : ValidationFailureReason() {

    override fun getDescription(): String {
      val duplicatedPortNumber = points.first().portNumber
      val duplicatedPointsNames = points.map { it.name }
      return "Following points have the same port number (=$duplicatedPortNumber): $duplicatedPointsNames"
    }
  }
}
