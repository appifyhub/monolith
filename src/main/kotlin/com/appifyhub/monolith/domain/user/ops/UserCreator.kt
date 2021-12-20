package com.appifyhub.monolith.domain.user.ops

import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.User.Type
import java.util.Date

data class UserCreator(
  val userId: String?, // some projects have pre-existing IDs
  val projectId: Long,
  val rawSecret: String,
  val name: String?,
  val type: Type,
  val authority: Authority,
  val allowsSpam: Boolean,
  val contact: String?,
  val contactType: ContactType,
  val birthday: Date?,
  val company: Organization?,
  val languageTag: String?,
)
