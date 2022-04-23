package com.appifyhub.monolith.network.messaging.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

// additional access verification is done in the controller
@JsonInclude(Include.NON_NULL)
data class MessageInputsRequest(
  @JsonProperty("user_id") val universalUserId: String? = null,
  @JsonProperty("project_id") val projectId: Long? = null,
) : Serializable
