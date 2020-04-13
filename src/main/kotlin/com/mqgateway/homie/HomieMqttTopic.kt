package com.mqgateway.homie

data class HomieMqttTopic(
  val root: String,
  val rootAttribute: String? = null,
  val deviceId: String? = null,
  val deviceAttribute: String? = null,
  val nodeId: String? = null,
  val nodeAttribute: String? = null,
  val propertyId: String? = null,
  val propertyAttribute: String? = null
) {

  companion object {
    fun fromString(topicString: String): HomieMqttTopic {
      val segments: List<String> = topicString.split('/')
      val root = segments[0]
      val rootAttribute = if (segments[1].startsWith("\$")) segments[1] else null
      val deviceId = if (!segments[1].startsWith("\$")) segments[1] else null
      var deviceAttribute: String? = null
      var nodeId: String? = null
      var nodeAttribute: String? = null
      var propertyId: String? = null
      var propertyAttribute: String? = null
      if (segments.size > 2) {
        deviceAttribute = if (segments[2].startsWith("\$")) segments[2] else null
        nodeId = if (!segments[2].startsWith("\$")) segments[2] else null
      }
      if (segments.size > 3) {
        nodeAttribute = if (segments[3].startsWith("\$")) segments[3] else null
        propertyId = if (!segments[3].startsWith("\$")) segments[3] else null
      }
      if (segments.size > 4) {
        propertyAttribute = segments[4]
      }

      return HomieMqttTopic(root, rootAttribute, deviceId, deviceAttribute, nodeId, nodeAttribute, propertyId, propertyAttribute)
    }
  }
}
