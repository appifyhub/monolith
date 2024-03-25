package com.appifyhub.monolith.features.common.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class SimpleResponse(
  @JsonProperty("message") val message: String,
) : Serializable {

  companion object {

    val DONE = SimpleResponse("Done")

  }

}
