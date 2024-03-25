package com.appifyhub.monolith.features.creator.domain.model

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.features.creator.domain.model.Project.Status
import com.appifyhub.monolith.features.creator.domain.model.Project.Type
import com.appifyhub.monolith.features.creator.domain.model.messaging.FirebaseConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.MailgunConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.TwilioConfig

data class ProjectUpdater(
  val id: Long,
  val type: Settable<Type>? = null,
  val status: Settable<Status>? = null,
  val name: Settable<String>? = null,
  val description: Settable<String?>? = null,
  val logoUrl: Settable<String?>? = null,
  val websiteUrl: Settable<String?>? = null,
  val maxUsers: Settable<Int>? = null,
  val anyoneCanSearch: Settable<Boolean>? = null,
  val onHold: Settable<Boolean>? = null,
  val languageTag: Settable<String?>? = null,
  val requiresSignupCodes: Settable<Boolean>? = null,
  val maxSignupCodesPerUser: Settable<Int>? = null,
  val mailgunConfig: Settable<MailgunConfig?>? = null,
  val twilioConfig: Settable<TwilioConfig?>? = null,
  val firebaseConfig: Settable<FirebaseConfig?>? = null,
)
