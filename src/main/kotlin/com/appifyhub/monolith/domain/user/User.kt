package com.appifyhub.monolith.domain.user

import java.util.Date
import org.springframework.security.core.GrantedAuthority

data class User(
  val id: UserId,
  val signature: String,
  val name: String? = null,
  val type: Type = Type.PERSONAL,
  val authority: Authority = Authority.DEFAULT,
  val allowsSpam: Boolean = false,
  val contact: String? = null,
  val contactType: ContactType = ContactType.CUSTOM,
  val verificationToken: String? = null,
  val birthday: Date? = null,
  val createdAt: Date,
  val updatedAt: Date = createdAt,
  val company: Organization? = null,
) {

  enum class Type {
    PERSONAL, ORGANIZATION;

    companion object {
      fun find(name: String, default: Type) =
        values().firstOrNull { it.name == name } ?: default
    }
  }

  enum class ContactType {
    EMAIL, PHONE, CUSTOM;

    companion object {
      fun find(name: String, default: ContactType) =
        values().firstOrNull { it.name == name } ?: default
    }
  }

  enum class Authority : GrantedAuthority {
    DEFAULT, MODERATOR, ADMIN, OWNER;

    companion object {

      fun find(name: String, default: Authority) =
        values().firstOrNull { it.name == name } ?: default

      fun find(granted: Collection<GrantedAuthority>, default: Authority) =
        ArrayList(granted)
          .map { find(it.authority, default = DEFAULT) }
          .maxByOrNull { it.ordinal }
          ?: default

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

  fun canActAs(authority: Authority) = this.authority.ordinal >= authority.ordinal

}
