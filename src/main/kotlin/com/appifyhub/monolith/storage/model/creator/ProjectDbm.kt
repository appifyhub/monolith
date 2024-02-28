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

  // region Basics

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

  @Column(nullable = true, length = 8, updatable = true)
  var languageTag: String?,

  @Column(nullable = false, updatable = true)
  var requiresSignupCodes: Boolean,

  @Column(nullable = false, updatable = true)
  var maxSignupCodesPerUser: Int,

  // endregion

  // region Mailgun

  @Column(nullable = true, length = 64, updatable = true)
  var mailgunApiKey: String?,

  @Column(nullable = true, length = 64, updatable = true)
  var mailgunDomain: String?,

  @Column(nullable = true, length = 32, updatable = true)
  var mailgunSenderName: String?,

  @Column(nullable = true, length = 64, updatable = true)
  var mailgunSenderEmail: String?,

  // endregion

  // region Twilio

  @Column(nullable = true, length = 64, updatable = true)
  val twilioAccountSid: String?,

  @Column(nullable = true, length = 64, updatable = true)
  val twilioAuthToken: String?,

  @Column(nullable = true, length = 64, updatable = true)
  val twilioMessagingServiceId: String?,

  @Column(nullable = true, updatable = true)
  val twilioMaxPricePerMessage: Int?,

  @Column(nullable = true, updatable = true)
  val twilioMaxRetryAttempts: Int?,

  @Column(nullable = true, length = 16, updatable = true)
  val twilioDefaultSenderName: String?,

  @Column(nullable = true, length = 32, updatable = true)
  val twilioDefaultSenderNumber: String?,

  // endregion

  // region Firebase

  @Column(nullable = true, length = 64, updatable = true)
  val firebaseProjectName: String?,

  @Column(nullable = true, length = 8192, updatable = true)
  val firebaseServiceAccountKeyJsonBase64: String?,

  // endregion

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date = createdAt,

) : Serializable {

  @Suppress("DuplicatedCode") // false positive
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ProjectDbm) return false

    if (projectId != other.projectId) return false
    if (type != other.type) return false
    if (status != other.status) return false
    if (userIdType != other.userIdType) return false
    if (languageTag != other.languageTag) return false
    if (requiresSignupCodes != other.requiresSignupCodes) return false
    if (maxSignupCodesPerUser != other.maxSignupCodesPerUser) return false

    if (mailgunApiKey != other.mailgunApiKey) return false
    if (mailgunDomain != other.mailgunDomain) return false
    if (mailgunSenderName != other.mailgunSenderName) return false
    if (mailgunSenderEmail != other.mailgunSenderEmail) return false

    if (twilioAccountSid != other.twilioAccountSid) return false
    if (twilioAuthToken != other.twilioAuthToken) return false
    if (twilioMessagingServiceId != other.twilioMessagingServiceId) return false
    if (twilioMaxPricePerMessage != other.twilioMaxPricePerMessage) return false
    if (twilioMaxRetryAttempts != other.twilioMaxRetryAttempts) return false
    if (twilioDefaultSenderName != other.twilioDefaultSenderName) return false
    if (twilioDefaultSenderNumber != other.twilioDefaultSenderNumber) return false

    if (firebaseProjectName != other.firebaseProjectName) return false
    if (firebaseServiceAccountKeyJsonBase64 != other.firebaseServiceAccountKeyJsonBase64) return false

    if (createdAt != other.createdAt) return false
    if (updatedAt != other.updatedAt) return false

    return true
  }

  @Suppress("DuplicatedCode") // false positive
  override fun hashCode(): Int {
    var result = projectId?.hashCode() ?: 0

    result = 31 * result + type.hashCode()
    result = 31 * result + status.hashCode()
    result = 31 * result + userIdType.hashCode()
    result = 31 * result + languageTag.hashCode()
    result = 31 * result + requiresSignupCodes.hashCode()
    result = 31 * result + maxSignupCodesPerUser.hashCode()

    result = 31 * result + mailgunApiKey.hashCode()
    result = 31 * result + mailgunDomain.hashCode()
    result = 31 * result + mailgunSenderName.hashCode()
    result = 31 * result + mailgunSenderEmail.hashCode()

    result = 31 * result + twilioAccountSid.hashCode()
    result = 31 * result + twilioAuthToken.hashCode()
    result = 31 * result + twilioMessagingServiceId.hashCode()
    result = 31 * result + twilioMaxPricePerMessage.hashCode()
    result = 31 * result + twilioMaxRetryAttempts.hashCode()
    result = 31 * result + twilioDefaultSenderName.hashCode()
    result = 31 * result + twilioDefaultSenderNumber.hashCode()

    result = 31 * result + firebaseProjectName.hashCode()
    result = 31 * result + firebaseServiceAccountKeyJsonBase64.hashCode()

    result = 31 * result + createdAt.hashCode()
    result = 31 * result + updatedAt.hashCode()

    return result
  }

  @Suppress("DuplicatedCode") // false positive
  override fun toString(): String {
    return "ProjectDbm(" +
      "projectId=$projectId, " +
      "type='$type', " +
      "status='$status', " +
      "userIdType='$userIdType', " +
      "languageTag='$languageTag', " +
      "requiresSignupCodes='$requiresSignupCodes', " +
      "maxSignupCodesPerUser='$maxSignupCodesPerUser', " +

      "mailgunApiKey='$mailgunApiKey', " +
      "mailgunDomain='$mailgunDomain', " +
      "mailgunSenderName='$mailgunSenderName', " +
      "mailgunSenderEmail='$mailgunSenderEmail', " +

      "twilioAccountSid='$twilioAccountSid', " +
      "twilioAuthToken='$twilioAuthToken', " +
      "twilioMessagingServiceId='$twilioMessagingServiceId', " +
      "twilioMaxPricePerMessage='$twilioMaxPricePerMessage', " +
      "twilioMaxRetryAttempts='$twilioMaxRetryAttempts', " +
      "twilioDefaultSenderName='$twilioDefaultSenderName', " +
      "twilioDefaultSenderNumber='$twilioDefaultSenderNumber', " +

      "firebaseProjectName='$firebaseProjectName', " +
      "firebaseServiceAccountKeyJsonBase64='$firebaseServiceAccountKeyJsonBase64', " +

      "createdAt=$createdAt, " +
      "updatedAt=$updatedAt" +
      ")"
  }

}
