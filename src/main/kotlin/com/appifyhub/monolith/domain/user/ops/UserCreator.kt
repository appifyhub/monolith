package com.appifyhub.monolith.domain.user.ops

import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.User.Type
import java.util.Date

data class UserCreator(
  val id: String?, // some projects have pre-existing IDs
  val projectId: Long,
  val rawSignature: String,
  val name: String? = null,
  val type: Type = Type.PERSONAL,
  val authority: Authority = Authority.DEFAULT,
  val allowsSpam: Boolean = false,
  val contact: String? = null,
  val contactType: ContactType = ContactType.CUSTOM,
  val birthday: Date? = null,
  val company: Organization? = null,
)