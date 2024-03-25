package com.appifyhub.monolith.features.creator.api.model.messaging

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class MessageResponse(
  @JsonProperty("template") val template: MessageTemplateResponse,
  @JsonProperty("materialized") val materialized: String,
) : Serializable
