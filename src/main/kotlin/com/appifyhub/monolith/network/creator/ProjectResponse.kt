package com.appifyhub.monolith.network.creator

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ProjectResponse(
  @JsonProperty("project_id") val projectId: Long,
  @JsonProperty("type") val type: String,
  @JsonProperty("status") val status: ProjectStatusDto,
  @JsonProperty("user_id_type") val userIdType: String,
  @JsonProperty("created_at") val createdAt: String,
  @JsonProperty("updated_at") val updatedAt: String,
) : Serializable
