package com.appifyhub.monolith.storage.model.creator

import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "project")
class ProjectDbm(

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(unique = true, nullable = false, updatable = false)
  var projectId: Long?,

  @Column(nullable = false, length = 16)
  var type: String,

  @Column(nullable = false, length = 16)
  var status: String,

  @Column(nullable = false, updatable = false, length = 16)
  var userIdType: String,

  @Column(nullable = false, updatable = true, length = 128)
  var name: String,

  @Column(nullable = true, updatable = true, length = 512)
  var description: String?,

  @Column(nullable = true, updatable = true, length = 1024)
  var logoUrl: String?,

  @Column(nullable = true, updatable = true, length = 1024)
  var websiteUrl: String?,

  @Column(nullable = false, updatable = true)
  var maxUsers: Int,

  @Column(nullable = false, updatable = true)
  var anyoneCanSearch: Boolean,

  @Column(nullable = false, updatable = true)
  var onHold: Boolean,

  @Column(nullable = true, length = 8)
  var languageTag: String?,

  @Column(nullable = true, length = 64)
  var mailgunApiKey: String?,

  @Column(nullable = true, length = 64)
  var mailgunDomain: String?,

  @Column(nullable = true, length = 32)
  var mailgunSenderName: String?,

  @Column(nullable = true, length = 64)
  var mailgunSenderEmail: String?,

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
    if (type != other.type) return false
    if (status != other.status) return false
    if (userIdType != other.userIdType) return false
    if (languageTag != other.languageTag) return false
    if (mailgunApiKey != other.mailgunApiKey) return false
    if (mailgunDomain != other.mailgunDomain) return false
    if (mailgunSenderName != other.mailgunSenderName) return false
    if (mailgunSenderEmail != other.mailgunSenderEmail) return false
    if (createdAt != other.createdAt) return false
    if (updatedAt != other.updatedAt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = projectId?.hashCode() ?: 0
    result = 31 * result + type.hashCode()
    result = 31 * result + status.hashCode()
    result = 31 * result + userIdType.hashCode()
    result = 31 * result + languageTag.hashCode()
    result = 31 * result + mailgunApiKey.hashCode()
    result = 31 * result + mailgunDomain.hashCode()
    result = 31 * result + mailgunSenderName.hashCode()
    result = 31 * result + mailgunSenderEmail.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + updatedAt.hashCode()
    return result
  }

  override fun toString(): String {
    return "ProjectDbm(" +
      "projectId=$projectId, " +
      "type='$type', " +
      "status='$status', " +
      "userIdType='$userIdType', " +
      "languageTag='$languageTag', " +
      "mailgunApiKey='$mailgunApiKey', " +
      "mailgunDomain='$mailgunDomain', " +
      "mailgunSenderName='$mailgunSenderName', " +
      "mailgunSenderEmail='$mailgunSenderEmail', " +
      "createdAt=$createdAt, " +
      "updatedAt=$updatedAt" +
      ")"
  }

}
