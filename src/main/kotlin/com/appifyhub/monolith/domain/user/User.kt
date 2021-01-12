package com.appifyhub.monolith.domain.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.BlockedToken
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
  val blockedTokens: List<BlockedToken> = emptyList(),
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

      fun find(granted: Collection<GrantedAuthority>) = ArrayList(granted)
        .map { find(it.authority, default = DEFAULT) }
        .maxByOrNull { it.ordinal }
        ?: DEFAULT

    }

    override fun getAuthority() = name

  }

  val allAuthorities: Array<Authority> =
    Authority.values().takeWhile { it.ordinal <= authority.ordinal }.toTypedArray()

  fun isAuthorizedFor(authority: Authority) = this.authority.ordinal >= authority.ordinal

  fun belongsTo(project: Project) = userId.projectId == project.id

}