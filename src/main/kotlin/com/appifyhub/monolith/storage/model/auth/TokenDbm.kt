package com.appifyhub.monolith.storage.model.auth

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class TokenDbm(

  @Column(unique = true, nullable = false, updatable = false, length = 1024)
  var tokenLocator: String,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TokenDbm) return false

    if (tokenLocator != other.tokenLocator) return false

    return true
  }

  override fun hashCode(): Int {
    return tokenLocator.hashCode()
  }

  override fun toString(): String {
    return "TokenDbm(" +
      "tokenLocator='$tokenLocator'" +
      ")"
  }

}
