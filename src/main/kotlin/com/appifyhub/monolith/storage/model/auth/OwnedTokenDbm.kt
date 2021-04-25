package com.appifyhub.monolith.storage.model.auth

import com.appifyhub.monolith.storage.model.user.UserDbm
import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "owned_token")
class OwnedTokenDbm(

  @Id
  @Column(unique = true, nullable = false, updatable = false, length = 1024)
  var tokenLocator: String,

  @Column(nullable = false)
  var blocked: Boolean,

  @Column(nullable = true, updatable = false, length = 256)
  var origin: String?,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var expiresAt: Date,

  @ManyToOne(fetch = FetchType.EAGER)
  var owner: UserDbm,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is OwnedTokenDbm) return false

    if (tokenLocator != other.tokenLocator) return false
    if (blocked != other.blocked) return false
    if (origin != other.origin) return false
    if (createdAt != other.createdAt) return false
    if (expiresAt != other.expiresAt) return false
    if (owner != other.owner) return false

    return true
  }

  override fun hashCode(): Int {
    var result = tokenLocator.hashCode()
    result = 31 * result + blocked.hashCode()
    result = 31 * result + (origin?.hashCode() ?: 0)
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + expiresAt.hashCode()
    result = 31 * result + owner.hashCode()
    return result
  }

  override fun toString(): String {
    return "OwnedTokenDbm(" +
      "tokenLocator='$tokenLocator', " +
      "blocked=$blocked, " +
      "origin=$origin, " +
      "createdAt=$createdAt, " +
      "expiresAt=$expiresAt, " +
      "owner=$owner" +
      ")"
  }

}
