package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.Point
import javax.inject.Singleton

@Singleton
class UniquePortNumbersForPointsValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val points: List<Point> = gatewayConfiguration.rooms.flatMap { room -> room.points }
    return points.groupBy { point -> point.portNumber }.filter { it.value.size > 1 }.values.toList().map { DuplicatedPortNumbersOnPoints(it) }
  }

  class DuplicatedPortNumbersOnPoints(val points: List<Point>) : ValidationFailureReason() {

    override fun getDescription(): String {
      val duplicatedPortNumber = points.first().portNumber
      val duplicatedPointsNames = points.map { it.name }
      return "Following points have the same port number (=$duplicatedPortNumber): $duplicatedPointsNames"
    }
  }
}
