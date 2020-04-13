package com.mqgateway.core.gatewayconfig

import kotlinx.serialization.Serializable

@Serializable
data class Room(
  val name: String,
  val points: List<Point>
)
