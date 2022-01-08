package com.appifyhub.monolith.domain.messaging

import com.appifyhub.monolith.domain.user.User as UserModel
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property

private const val DEFAULT_VALUE = "?"

sealed interface TemplateDataBinder<S> {

  enum class Code(val code: String) {
    USER_NAME("user.name"),

    PROJECT_NAME("project.name"),

    ;

    companion object {
      fun find(code: String, default: Code? = null) =
        values().firstOrNull { it.code == code }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

  // User data binders

  object User {

    object Name : TemplateDataBinder<UserModel> {
      override fun bind(source: UserModel) = source.name ?: DEFAULT_VALUE
    }

  }

  // Project data binders

  object Project {

    @Suppress("UNCHECKED_CAST")
    object Name : TemplateDataBinder<List<Property<*>>> {
      override fun bind(source: List<Property<*>>) =
        source.firstOrNull { it.config == ProjectProperty.NAME }
          ?.let { it as? Property<String> }
          ?.typed()
          ?: DEFAULT_VALUE
    }

  }

  // Implementation requirements

  fun bind(source: S): String

}
