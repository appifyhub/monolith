package com.appifyhub.monolith.domain.admin

import java.util.Date

data class Project(
  val id: Long,
  val account: Account,
  val name: String,
  val type: Type = Type.COMMERCIAL,
  val status: Status = Status.REVIEW,
  val userIdType: UserIdType = UserIdType.RANDOM,
  val createdAt: Date,
  val updatedAt: Date = createdAt,
) {

  enum class UserIdType {
    USERNAME, EMAIL, PHONE, RANDOM, CUSTOM;

    companion object {
      fun find(name: String, default: UserIdType) =
        values().firstOrNull { it.name == name } ?: default
    }
  }

  enum class Type {
    OPENSOURCE, COMMERCIAL, FREE;

    companion object {
      fun find(name: String, default: Type) =
        values().firstOrNull { it.name == name } ?: default
    }
  }

  enum class Status {
    REVIEW, ACTIVE, BLOCKED, SUSPENDED;

    companion object {
      fun find(name: String, default: Status) =
        values().firstOrNull { it.name == name } ?: default
    }
  }

}
