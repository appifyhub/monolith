package com.appifyhub.monolith.storage.model.admin

import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "project")
class ProjectDbm(

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(unique = true, nullable = false, updatable = false)
  var projectId: Long?,

  @ManyToOne(fetch = FetchType.EAGER)
  var account: AccountDbm,

  @Column(nullable = false, length = 32)
  var name: String,

  @Column(nullable = false, length = 16)
  var type: String,

  @Column(nullable = false, length = 16)
  var status: String,

  @Column(nullable = false, updatable = false, length = 16)
  var userIdType: String,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date = createdAt,

) : Serializable {

  @Suppress("DuplicatedCode")
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ProjectDbm) return false

    if (projectId != other.projectId) return false
    if (account != other.account) return false
    if (name != other.name) return false
    if (type != other.type) return false
    if (status != other.status) return false
    if (userIdType != other.userIdType) return false
    if (createdAt != other.createdAt) return false
    if (updatedAt != other.updatedAt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = projectId?.hashCode() ?: 0
    result = 31 * result + account.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + status.hashCode()
    result = 31 * result + userIdType.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + updatedAt.hashCode()
    return result
  }

  override fun toString(): String {
    return "ProjectDbm(" +
      "projectId=$projectId, " +
      "account=$account, " +
      "name='$name', " +
      "type='$type', " +
      "status='$status', " +
      "userIdType='$userIdType', " +
      "createdAt=$createdAt, " +
      "updatedAt=$updatedAt" +
      ")"
  }

}
