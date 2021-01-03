package com.appifyhub.monolith.domain.user

data class UserId(
  val id: String,
  val projectId: Long,
) {

  companion object {
    private const val UNIFIED_DELIMITER = "$"

    @Throws(IllegalArgumentException::class, NumberFormatException::class, NoSuchElementException::class)
    fun fromUnifiedFormat(idHashProjectId: String): UserId {
      val (id, projectId) = idHashProjectId.split(UNIFIED_DELIMITER).let { it.first() to it.last() }
      if (id.isBlank() || projectId.isBlank()) throw IllegalArgumentException("ID and Project ID can't be blank")
      return UserId(id = id.trim(), projectId = projectId.trim().toLong())
    }
  }

  fun toUnifiedFormat() = "$id$UNIFIED_DELIMITER$projectId"

}