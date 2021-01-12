package com.appifyhub.monolith.network.user

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class OrganizationDto(
  @JsonProperty("name") val name: String? = null,
  @JsonProperty("street") val street: String? = null,
  @JsonProperty("postcode") val postcode: String? = null,
  @JsonProperty("city") val city: String? = null,
  @JsonProperty("country_code") val countryCode: String? = null,
) : Serializable