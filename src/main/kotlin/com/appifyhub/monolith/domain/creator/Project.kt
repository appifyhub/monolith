package com.appifyhub.monolith.domain.creator

import java.util.Date

data class Project(
  val id: Long,
  val type: Type = Type.COMMERCIAL,
  val status: Status = Status.REVIEW,
  val userIdType: UserIdType = UserIdType.RANDOM,
  val createdAt: Date,
  val updatedAt: Date = createdAt,
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
