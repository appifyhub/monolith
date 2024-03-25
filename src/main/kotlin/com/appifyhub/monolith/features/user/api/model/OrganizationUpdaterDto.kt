package com.appifyhub.monolith.features.user.api.model

import com.appifyhub.monolith.features.common.api.model.SettableRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class OrganizationUpdaterDto(
  @JsonProperty("name") val name: SettableRequest<String?>? = null,
  @JsonProperty("street") val street: SettableRequest<String?>? = null,
  @JsonProperty("postcode") val postcode: SettableRequest<String?>? = null,
  @JsonProperty("city") val city: SettableRequest<String?>? = null,
  @JsonProperty("country_code") val countryCode: SettableRequest<String?>? = null,
) : Serializable
