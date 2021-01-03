package com.appifyhub.monolith.domain.admin

import java.util.Date

data class Project(
  val id: Long,
  val account: Account,
  val signature: String,
  val name: String,
  val type: Type = Type.COMMERCIAL,
  val status: Status = Status.REVIEW,
  val createdAt: Date,
  val updatedAt: Date = createdAt,
) {

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



