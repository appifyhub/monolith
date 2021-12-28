package com.appifyhub.monolith.domain.user

import java.util.Date
import org.springframework.security.core.GrantedAuthority

data class User(
  val id: UserId,
  val signature: String,
  val name: String?,
  val type: Type,
  val authority: Authority,
  val allowsSpam: Boolean,
  val contact: String?,
  val contactType: ContactType,
  val verificationToken: String?,
  val birthday: Date?,
  val company: Organization?,
  val languageTag: String?,
  val createdAt: Date,
  val updatedAt: Date,
) {

  enum class Type {
    PERSONAL, ORGANIZATION;

    companion object {
      fun find(name: String, default: Type? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

  enum class ContactType {
    EMAIL, PHONE, CUSTOM;

    companion object {
      fun find(name: String, default: ContactType? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

  enum class Authority : GrantedAuthority {
    DEFAULT, MODERATOR, ADMIN, OWNER;

    companion object {

      fun find(name: String, default: Authority? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")

      fun find(granted: Collection<GrantedAuthority>, default: Authority? = null) =
        ArrayList(granted)
          .map { find(it.authority, default = DEFAULT) }
          .maxByOrNull { it.ordinal }
          ?: default
          ?: throw IllegalArgumentException("Not found")

    }

    override fun getAuthority() = name

    // ADMIN -> admins
    val groupName: String by lazy { name.lowercase().plus("s") }

    // DEFAULT -> moderators
    // OWNER -> gods
    val nextGroupName: String by lazy { values().getOrNull(ordinal + 1)?.groupName ?: "gods" }

    // MODERATOR -> [DEFAULT, MODERATOR]
    val allAuthorities: List<Authority> by lazy { values().takeWhile { it.ordinal <= this.ordinal } }

  }

  val allAuthorities = authority.allAuthorities

  val isVerified = verificationToken.isNullOrBlank()

}
