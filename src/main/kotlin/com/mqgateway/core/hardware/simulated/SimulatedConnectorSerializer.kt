package com.mqgateway.core.hardware.simulated

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer

@ExperimentalSerializationApi
@Serializer(forClass = SimulatedConnector::class)
object SimulatedConnectorSerializer
