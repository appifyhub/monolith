package com.appifyhub.monolith.features.creator.domain.model

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.features.creator.domain.model.Project.Status
import com.appifyhub.monolith.features.creator.domain.model.Project.Type
import com.appifyhub.monolith.features.creator.domain.model.Project.UserIdType
import com.appifyhub.monolith.features.creator.domain.model.messaging.FirebaseConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.MailgunConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.TwilioConfig

data class ProjectCreator(
  val owner: User?,
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
)
