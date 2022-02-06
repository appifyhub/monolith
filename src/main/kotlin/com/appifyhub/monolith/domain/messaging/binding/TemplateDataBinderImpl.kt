package com.appifyhub.monolith.domain.messaging.binding

import com.appifyhub.monolith.domain.messaging.binding.TemplateDataBinder.Code
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.creator.CreatorRepository
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.util.ext.silent
import org.springframework.stereotype.Component

private const val DEFAULT_VALUE = "******"

@Component
class TemplateDataBinderImpl(
  private val userRepository: UserRepository,
  private val creatorRepository: CreatorRepository,
) : TemplateDataBinder {

  override fun bind(
    code: Code,
    userId: UserId?,
    projectId: Long?,
  ): String = silent {
    when (code) {

      Code.USER_NAME -> userRepository.fetchUserByUserId(requireNotNull(userId)).name

      Code.PROJECT_NAME -> creatorRepository.fetchProjectById(requireNotNull(projectId)).name

    }
  } ?: DEFAULT_VALUE

}
