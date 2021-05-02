package com.appifyhub.monolith.storage.model.auth

import com.appifyhub.monolith.storage.model.user.UserDbm
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity(name = "token_details")
class TokenDetailsDbm(

  @Id
  @Column(unique = true, nullable = false, updatable = false, length = 2048)
  var tokenValue: String,

  @Column(nullable = false)
  var blocked: Boolean,

  @ManyToOne(fetch = FetchType.EAGER)
  var owner: UserDbm,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TokenDetailsDbm) return false

    if (tokenValue != other.tokenValue) return false
    if (blocked != other.blocked) return false
    if (owner != other.owner) return false

    return true
  }

  override fun hashCode(): Int {
    var result = tokenValue.hashCode()
    result = 31 * result + blocked.hashCode()
    result = 31 * result + owner.hashCode()
    return result
  }

  override fun toString(): String {
    return "TokenDetailsDbm(" +
      "tokenValue='$tokenValue', " +
      "blocked=$blocked, " +
      "owner=$owner" +
      ")"
  }

}
