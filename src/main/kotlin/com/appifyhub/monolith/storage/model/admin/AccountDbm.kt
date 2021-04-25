package com.appifyhub.monolith.storage.model.admin

import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "account")
class AccountDbm(

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(unique = true, nullable = false, updatable = false)
  var accountId: Long?,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date = createdAt,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AccountDbm) return false

    if (accountId != other.accountId) return false
    if (createdAt != other.createdAt) return false
    if (updatedAt != other.updatedAt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = accountId?.hashCode() ?: 0
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + updatedAt.hashCode()
    return result
  }

  override fun toString(): String {
    return "AccountDbm(" +
      "accountId=$accountId, " +
      "createdAt=$createdAt, " +
      "updatedAt=$updatedAt" +
      ")"
  }

}
