package com.appifyhub.monolith.storage.model.user

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class OrganizationDbm(

  @Column(name = "orga_name", nullable = true, length = 64)
  var name: String?,

  @Column(name = "orga_street", nullable = true, length = 128)
  var street: String?,

  @Column(name = "orga_postcode", nullable = true, length = 16)
  var postcode: String?,

  @Column(name = "orga_city", nullable = true, length = 128)
  var city: String?,

  @Column(name = "orga_country_code", nullable = true, length = 4)
  var countryCode: String?,

) : Serializable
