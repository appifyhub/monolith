package com.appifyhub.monolith.features.creator.domain.model

import com.appifyhub.monolith.features.creator.domain.model.messaging.FirebaseConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.MailgunConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.TwilioConfig
import java.util.Date

data class Project(
  val id: Long,
  val type: Type,
  val status: Status,
  val userIdType: UserIdType,
  val name: String,
  val description: String?,
  val logoUrl: String?,
  val websiteUrl: String?,
  val maxUsers: Int,
  val anyoneCanSearch: Boolean,
  val onHold: Boolean,
  val languageTag: String?,
  var requiresSignupCodes: Boolean,
  var maxSignupCodesPerUser: Int,
  val mailgunConfig: MailgunConfig?,
  val twilioConfig: TwilioConfig?,
  val firebaseConfig: FirebaseConfig?,
  val createdAt: Date,
  val updatedAt: Date,
) {

  enum class UserIdType {
    USERNAME, EMAIL, PHONE, RANDOM, CUSTOM;

    companion object {
      fun find(name: String, default: UserIdType? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

  enum class Type {
    OPENSOURCE, COMMERCIAL, FREE;

    companion object {
      fun find(name: String, default: Type? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

  enum class Status {
    REVIEW, ACTIVE, BLOCKED, SUSPENDED;

    companion object {
      fun find(name: String, default: Status? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

}
