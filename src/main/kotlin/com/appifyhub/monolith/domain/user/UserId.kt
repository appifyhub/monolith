package com.appifyhub.monolith.domain.user

data class UserId(
  val id: String,
  val projectId: Long,
) {

  companion object {
    private const val UNIVERSAL_DELIMITER = "$"

    @Throws(IllegalArgumentException::class, NumberFormatException::class, NoSuchElementException::class)
    fun fromUniversalFormat(universalId: String): UserId {
      val (userId, projectId) = universalId.split(UNIVERSAL_DELIMITER).let { it.first() to it.last() }
      if (userId.isBlank() || projectId.isBlank()) throw IllegalArgumentException("ID and Project ID can't be blank")
      return UserId(id = userId.trim(), projectId = projectId.trim().toLong())
    }
  }

  fun toUniversalFormat() = "$id$UNIVERSAL_DELIMITER$projectId"

}
