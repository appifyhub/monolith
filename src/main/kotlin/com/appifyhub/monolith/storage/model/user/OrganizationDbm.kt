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

) : Serializable {

  @Suppress("DuplicatedCode") // false positive
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is OrganizationDbm) return false

    if (name != other.name) return false
    if (street != other.street) return false
    if (postcode != other.postcode) return false
    if (city != other.city) return false
    if (countryCode != other.countryCode) return false

    return true
  }

  @Suppress("DuplicatedCode") // false positive
  override fun hashCode(): Int {
    var result = name?.hashCode() ?: 0
    result = 31 * result + (street?.hashCode() ?: 0)
    result = 31 * result + (postcode?.hashCode() ?: 0)
    result = 31 * result + (city?.hashCode() ?: 0)
    result = 31 * result + (countryCode?.hashCode() ?: 0)
    return result
  }

  @Suppress("DuplicatedCode") // false positive
  override fun toString(): String {
    return "OrganizationDbm(" +
      "name=$name, " +
      "street=$street, " +
      "postcode=$postcode, " +
      "city=$city, " +
      "countryCode=$countryCode" +
      ")"
  }

}
