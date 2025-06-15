package com.mqgateway.core.hardware.mqgateway.validators

import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import com.mqgateway.core.hardware.mqgateway.MqGatewayConnector
import com.mqgateway.core.hardware.mqgateway.MqGatewayPlatformConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class PortNumbersRangeValidator(private val platformConfiguration: MqGatewayPlatformConfiguration) : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    LOGGER.debug { "Validating that connectors port numbers are in range" }

    val expanderEnabled = platformConfiguration.expander.enabled
    val maxPortNumber = maxPortNumber(expanderEnabled)
    return gatewayConfiguration.devices.flatMap { device ->
      device.connectors.filter { (_, connector) -> connector is MqGatewayConnector }
        .map { (connectorName, connector) ->
          val mqGatewayConnector = connector as MqGatewayConnector
          Pair(Pair(device.id, connectorName), mqGatewayConnector.portNumber)
        }
    }.toMap().filter { it.value > maxPortNumber || it.value < 1 }.map {
      val (deviceId, connectorName) = it.key
      val portNumber = it.value
      PortNumberOutOfRange(deviceId, connectorName, portNumber, expanderEnabled)
    }
  }

  data class PortNumberOutOfRange(
    val deviceId: String,
    val connectorName: String,
    val portNumber: Int,
    val expanderEnabled: Boolean,
  ) : ValidationFailureReason() {
    override fun getDescription(): String {
      val expanderDisabledInfo = if (!expanderEnabled) " It may be because expander is disabled in system configuration." else ""
      return "Device '$deviceId' has port number '$portNumber' which is out of range [1, ${maxPortNumber(expanderEnabled)}]." + expanderDisabledInfo
    }
  }

  companion object {
    fun maxPortNumber(expanderEnabled: Boolean) = if (expanderEnabled) 32 else 16
  }
}
