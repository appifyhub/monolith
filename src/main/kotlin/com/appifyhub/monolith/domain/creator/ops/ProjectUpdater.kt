package com.appifyhub.monolith.domain.creator.ops

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project.Status
import com.appifyhub.monolith.domain.creator.Project.Type

data class ProjectUpdater(
  val id: Long,
  val type: Settable<Type>? = null,
  val status: Settable<Status>? = null,
  val languageTag: Settable<String?>? = null,
)
