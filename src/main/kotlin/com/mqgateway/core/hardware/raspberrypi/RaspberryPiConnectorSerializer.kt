package com.mqgateway.core.hardware.raspberrypi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer

@ExperimentalSerializationApi
@Serializer(forClass = RaspberryPiConnector::class)
object RaspberryPiConnectorSerializer
