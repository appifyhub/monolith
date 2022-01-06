package com.appifyhub.monolith.domain.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.storage.model.creator.PropertyDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.Test
import java.util.Date

class CreatorMapperTest {

  @Test fun `project updater to project (no changes)`() {
    val projectUpdater = ProjectUpdater(
      id = Stubs.project.id,
      type = null,
      status = null,
    )

    val result = projectUpdater.applyTo(
      project = Stubs.project,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.project.copy(
        updatedAt = Date(0),
      )
    )
  }

  @Test fun `project updater to project (with changes)`() {
    val projectUpdater = ProjectUpdater(
      id = Stubs.project.id,
      type = Settable(Project.Type.COMMERCIAL),
      status = Settable(Project.Status.SUSPENDED),
    )

    val result = projectUpdater.applyTo(
      project = Stubs.project,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.project.copy(
        type = Project.Type.COMMERCIAL,
        status = Project.Status.SUSPENDED,
        updatedAt = Date(0),
      )
    )
  }

  @Test fun `project creator to project data`() {
    // based on stub data
    val startTime = Stubs.projectDbm.createdAt.time
    val timeIncrement = Stubs.projectDbm.updatedAt.time - startTime
    val timeProvider = TimeProviderFake(incrementalTime = startTime, timeIncrement = timeIncrement)

    val projectDbm = Stubs.projectCreator.toProjectData(
      timeProvider = timeProvider,
    ).apply {
      // no info about IDs from this conversion
      projectId = Stubs.projectDbm.projectId
    }

    assertThat(projectDbm).isEqualTo(Stubs.projectDbm)
  }

  @Test fun `project data to domain`() {
    assertThat(Stubs.projectDbm.toDomain()).isDataClassEqualTo(Stubs.project)
  }

  @Test fun `project domain to data`() {
    assertThat(Stubs.project.toData()).isEqualTo(Stubs.projectDbm)
  }

  @Test fun `property data to domain`() {
    val propsData = listOf(Stubs.propStringDbm, Stubs.propIntegerDbm, Stubs.propDecimalDbm, Stubs.propFlagDbm)
    val propsDomain = listOf(Stubs.propString, Stubs.propInteger, Stubs.propDecimal, Stubs.propFlag)

    assertThat(propsData.map(PropertyDbm::toDomain))
      .isEqualTo(propsDomain)
  }

  @Test fun `property domain to data`() {
    val propsDomain = listOf(Stubs.propString, Stubs.propInteger, Stubs.propDecimal, Stubs.propFlag)
    val propsData = listOf(Stubs.propStringDbm, Stubs.propIntegerDbm, Stubs.propDecimalDbm, Stubs.propFlagDbm)

    assertThat(propsDomain.map { it.toData(Stubs.project) })
      .isEqualTo(propsData)
  }

}
