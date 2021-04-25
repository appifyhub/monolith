package com.appifyhub.monolith.domain.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.auth.OwnedToken
import org.springframework.security.core.GrantedAuthority
import java.util.Date

data class User(
  val userId: UserId,
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
  val ownedTokens: List<OwnedToken> = emptyList(),
  val account: Account? = null,
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
    val groupName: String by lazy { name.toLowerCase().plus("s") }

    // DEFAULT -> moderators
    // OWNER -> gods
    val nextGroupName: String by lazy { values().getOrNull(ordinal + 1)?.groupName ?: "gods" }

  }

  val allAuthorities = Authority.values().takeWhile { it.ordinal <= authority.ordinal }

  fun isAuthorizedFor(authority: Authority) = this.authority.ordinal >= authority.ordinal

}
