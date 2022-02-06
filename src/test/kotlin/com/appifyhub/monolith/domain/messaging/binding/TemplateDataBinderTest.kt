package com.appifyhub.monolith.domain.messaging.binding

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.domain.messaging.binding.TemplateDataBinder.Code
import com.appifyhub.monolith.repository.creator.CreatorRepository
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

class TemplateDataBinderTest {

  private val userRepo = mock<UserRepository>()
  private val creatorRepo = mock<CreatorRepository>()

  private val binder: TemplateDataBinder = TemplateDataBinderImpl(
    userRepository = userRepo,
    creatorRepository = creatorRepo,
  )

  @Test fun `user's name can bind properly`() {
    userRepo.stub {
      onGeneric { fetchUserByUserId(Stubs.userId) } doReturn Stubs.user
    }

    assertThat(binder.bind(Code.USER_NAME, userId = Stubs.userId))
      .isEqualTo(Stubs.user.name)
  }

  @Test fun `project's name can bind properly`() {
    creatorRepo.stub {
      onGeneric { fetchProjectById(Stubs.project.id) } doReturn Stubs.project
    }

    assertThat(binder.bind(Code.PROJECT_NAME, projectId = Stubs.project.id))
      .isEqualTo(Stubs.project.name)
  }

}
