package com.appifyhub.monolith.domain.creator

import java.util.Date

data class Project(
  val id: Long,
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
  val createdAt: Date,
  val updatedAt: Date,
) {

  enum class UserIdType {
    USERNAME, EMAIL, PHONE, RANDOM, CUSTOM;

    companion object {
      fun find(name: String, default: UserIdType? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

  enum class Type {
    OPENSOURCE, COMMERCIAL, FREE;

    companion object {
      fun find(name: String, default: Type? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

  enum class Status {
    REVIEW, ACTIVE, BLOCKED, SUSPENDED;

    companion object {
      fun find(name: String, default: Status? = null) =
        values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

}
