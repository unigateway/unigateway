package com.unigateway.core.hardware.mqgateway.validators

import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.gatewayconfig.validation.GatewayValidator
import com.unigateway.core.gatewayconfig.validation.ValidationFailureReason
import com.unigateway.core.hardware.mqgateway.MqGatewayConnector
import com.unigateway.core.hardware.mqgateway.WireColor
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class UniqueWiresValidatorGatewayValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    LOGGER.debug { "Validating that connectors use unique wires among all devices" }

    return gatewayConfiguration.devices.flatMap { device ->
      device.connectors.filter { (_, connector) -> connector is MqGatewayConnector }.map { (connectorName, connector) ->
        val mqGatewayConnector = connector as MqGatewayConnector
        Pair(Pair(device.id, connectorName), Pair(mqGatewayConnector.portNumber, mqGatewayConnector.wireColor))
      }
    }
      .groupBy { (_, portNumberAndWireColor) -> portNumberAndWireColor }
      .filter { samePortNumberAndWireColorGroup -> samePortNumberAndWireColorGroup.value.size > 1 }.map { (group, value) ->
        val (portNumber, wireColor) = group
        val connectorsList = value.map { it.first }
        MqGatewayWiresNotUnique(portNumber, wireColor, connectorsList)
      }
  }

  data class MqGatewayWiresNotUnique(
    val portNumber: Int,
    val wireColor: WireColor,
    private val connectorsOnDevices: List<Pair<String, String>>
  ) : ValidationFailureReason() {

    override fun getDescription(): String {
      val connectorsListString = connectorsOnDevices.joinToString(", ") { "<deviceId: ${it.first}, connector: ${it.second}>" }
      return "Configuration contains the same portNumber and wireColor ($portNumber, $wireColor) on the following connectors: $connectorsListString"
    }
  }
}
