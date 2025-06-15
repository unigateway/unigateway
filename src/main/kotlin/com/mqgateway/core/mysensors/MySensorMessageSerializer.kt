package com.mqgateway.core.mysensors

class MySensorMessageSerializer {
  fun deserialize(messageString: String): Message {
    val parts = messageString.split(";")
    val (nodeIdString, childSensorIdString, commandId, ackString, typeString, payload) = parts
    val command: Command = Command.byId(commandId.toByte()) ?: throw UnknownCommandException(commandId)
    val type: Type =
      when (command) {
        Command.PRESENTATION -> PresentationType.byId(typeString.toByte())
        Command.SET -> SetReqType.byId(typeString.toByte())
        Command.REQ -> SetReqType.byId(typeString.toByte())
        Command.INTERNAL -> InternalType.byId(typeString.toByte())
        Command.STREAM -> StreamType.STREAM
      } ?: throw UnknownCommandTypeException(command, typeString)

    return Message(nodeIdString.toInt(), childSensorIdString.toInt(), command, ackString == "1", type, payload.trim())
  }

  fun serialize(message: Message): String {
    return listOf(
      message.nodeId.toString(),
      message.childSensorId.toString(),
      message.command.id.toString(),
      if (message.ack) "1" else "0",
      message.type.getId().toString(),
      message.payload,
    ).joinToString(";", "", "\n")
  }
}

private operator fun <E> List<E>.component6(): E = get(5)

class UnknownCommandException(commandId: String) : Exception("Unknown commandId '$commandId'")

class UnknownCommandTypeException(command: Command, typeString: String) : RuntimeException("Unknown type '$typeString' for command '$command'")
