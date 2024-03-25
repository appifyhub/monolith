package com.appifyhub.monolith.features.user.domain.model

import com.appifyhub.monolith.features.user.domain.model.User.Authority
import com.appifyhub.monolith.features.user.domain.model.User.ContactType
import com.appifyhub.monolith.features.user.domain.model.User.Type
import java.util.Date

data class UserCreator(
  val userId: String?, // some projects have pre-existing IDs
  val projectId: Long,
  val rawSignature: String,
  val name: String?,
  val type: Type,
  val authority: Authority,
  val allowsSpam: Boolean,
  val contact: String?,
  val contactType: ContactType,
  val birthday: Date?,
  val company: Organization?,
  val languageTag: String?,
  val signupCode: String?,
)
