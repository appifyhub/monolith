package com.appifyhub.monolith.service.integrations

import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.User

interface CommunicationsService {

  enum class Type {
    EMAIL, SMS, PUSH;

    companion object {
      fun find(name: String, default: Type? = null) =
        Type.values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

  @Throws fun sendTo(projectId: Long, userId: UserId, overrideUser: User? = null, templateId: Long, type: Type)

  @Throws fun sendTo(projectId: Long, userId: UserId, overrideUser: User? = null, templateName: String, type: Type)

}
