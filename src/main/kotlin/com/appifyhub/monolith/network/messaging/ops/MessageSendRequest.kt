package com.appifyhub.monolith.network.messaging.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class MessageSendRequest(
  @JsonProperty("message_type") val type: String,
  @JsonProperty("message_template_id") val templateId: Long? = null, // either this or the name below
  @JsonProperty("message_template_name") val templateName: String? = null, // see the comment above
) : Serializable
