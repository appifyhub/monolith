package com.appifyhub.monolith.network.integrations

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class FirebaseConfigDto(
  @JsonProperty("project_name") val projectName: String,
  @JsonProperty("service_account_key_json_base64") val serviceAccountKeyJsonBase64: String,
)
