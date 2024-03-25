package com.appifyhub.monolith.features.creator.api

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class CreatorProjectOpsMapperTest {

  @Test fun `project creator - network to domain (with user)`() {
    assertThat(Stubs.projectCreateRequest.toDomain(owner = Stubs.user))
      .isDataClassEqualTo(
        Stubs.projectCreator.copy(
          owner = Stubs.user,
          status = Project.Status.REVIEW,
        )
      )
  }

  @Test fun `project creator - network to domain (without user)`() {
    assertThat(Stubs.projectCreateRequest.toDomain())
      .isDataClassEqualTo(
        Stubs.projectCreator.copy(
          status = Project.Status.REVIEW,
        )
      )
  }

  @Test fun `project updater - network to domain`() {
    assertThat(Stubs.projectUpdateRequest.toDomain(Stubs.project.id))
      .isDataClassEqualTo(Stubs.projectUpdater.copy())
  }

}
