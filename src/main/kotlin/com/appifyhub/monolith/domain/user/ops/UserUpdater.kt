package com.appifyhub.monolith.domain.user.ops

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.User.Type
import com.appifyhub.monolith.domain.user.UserId
import java.util.Date

data class UserUpdater(
  val id: UserId,

  // cannot be set to null (can be skipped)
  val rawSignature: Settable<String>? = null,
  val type: Settable<Type>? = null,
  val authority: Settable<Authority>? = null,
  val contactType: Settable<ContactType>? = null,
  val allowsSpam: Settable<Boolean>? = null,

  // can be set to null (and also skipped)
  val name: Settable<String?>? = null,
  val contact: Settable<String?>? = null,
  val verificationToken: Settable<String?>? = null,
  val birthday: Settable<Date?>? = null,
  val company: Settable<OrganizationUpdater?>? = null,
)
