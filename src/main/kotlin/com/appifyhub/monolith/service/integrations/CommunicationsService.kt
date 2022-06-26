package com.appifyhub.monolith.service.integrations

import com.appifyhub.monolith.domain.user.UserId

interface CommunicationsService {

  enum class Type {
    EMAIL,
  }

  @Throws fun sendTo(projectId: Long, userId: UserId, templateId: Long, type: Type)

  @Throws fun sendTo(projectId: Long, userId: UserId, templateName: String, type: Type)

}
