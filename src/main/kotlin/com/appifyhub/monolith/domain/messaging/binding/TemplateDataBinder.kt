package com.appifyhub.monolith.domain.messaging.binding

import com.appifyhub.monolith.domain.user.UserId

interface TemplateDataBinder {

  enum class Code(val code: String) {

    USER_NAME("user.name"),

    PROJECT_NAME("project.name"),

    ;

    companion object {
      fun findByCode(code: String, default: Code? = null) =
        values().firstOrNull { it.code == code }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }

  }

  fun bind(
    code: Code,
    userId: UserId? = null,
    projectId: Long? = null,
  ): String

}
