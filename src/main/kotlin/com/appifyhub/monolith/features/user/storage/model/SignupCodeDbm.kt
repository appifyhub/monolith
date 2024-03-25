package com.appifyhub.monolith.features.user.storage.model

import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "signup_code")
class SignupCodeDbm(

  @Id
  @Column(unique = true, nullable = false, updatable = false, length = 128)
  var code: String,

  @Column(nullable = false)
  var isUsed: Boolean, // redundant but useful for querying

  @ManyToOne(fetch = FetchType.EAGER)
  var owner: UserDbm,

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  var createdAt: Date,

  @Column(nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  var usedAt: Date?,

) : Serializable {

  @Suppress("DuplicatedCode") // false positive
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SignupCodeDbm) return false

    if (code != other.code) return false
    if (isUsed != other.isUsed) return false
    if (owner != other.owner) return false
    if (createdAt != other.createdAt) return false
    if (usedAt != other.usedAt) return false

    return true
  }

  @Suppress("DuplicatedCode") // false positive
  override fun hashCode(): Int {
    var result = code.hashCode()
    result = 31 * result + isUsed.hashCode()
    result = 31 * result + owner.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + usedAt.hashCode()
    return result
  }

  @Suppress("DuplicatedCode") // false positive
  override fun toString(): String {
    return "SignupCodeDbm(" +
      "code='$code', " +
      "isUsed=$isUsed, " +
      "owner=$owner, " +
      "createdAt=$createdAt, " +
      "usedAt=$usedAt" +
      ")"
  }

}
