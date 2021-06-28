package com.appifyhub.monolith.domain.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountOwnerUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.storage.model.admin.PropertyDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import java.util.Date
import org.junit.jupiter.api.Test

class AdminMapperTest {

  @Test fun `account updater to account (no changes)`() {
    val accountUpdater = AccountOwnerUpdater(
      id = Stubs.account.id,
      addedOwners = null,
      removedOwners = null,
    )

    val result = accountUpdater.applyTo(
      account = Stubs.account,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.account.copy(
        updatedAt = Date(0),
      )
    )
  }

  @Test fun `account updater to account (added owners)`() {
    val newOwner = Stubs.user.copy(
      id = Stubs.userId.copy(userId = "u1"),
    )
    val accountUpdater = AccountOwnerUpdater(
      id = Stubs.account.id,
      addedOwners = Settable(listOf(newOwner)),
      removedOwners = null,
    )

    val result = accountUpdater.applyTo(
      account = Stubs.account,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.account.copy(
        updatedAt = Date(0),
        owners = listOf(Stubs.user, newOwner),
      )
    )
  }

  @Test fun `account updater to account (removed owners)`() {
    val accountUpdater = AccountOwnerUpdater(
      id = Stubs.account.id,
      addedOwners = null,
      removedOwners = Settable(listOf(Stubs.user)),
    )

    val result = accountUpdater.applyTo(
      account = Stubs.account,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.account.copy(
        updatedAt = Date(0),
        owners = emptyList(),
      )
    )
  }

  @Test fun `account updater to account (added and removed owners)`() {
    val newOwner = Stubs.user.copy(
      id = Stubs.userId.copy(userId = "u1"),
    )
    val accountUpdater = AccountOwnerUpdater(
      id = Stubs.account.id,
      addedOwners = Settable(listOf(newOwner)),
      removedOwners = Settable(listOf(Stubs.user)),
    )

    val result = accountUpdater.applyTo(
      account = Stubs.account,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.account.copy(
        updatedAt = Date(0),
        owners = listOf(newOwner),
      )
    )
  }

  @Test fun `project updater to project (no changes)`() {
    val projectUpdater = ProjectUpdater(
      id = Stubs.project.id,
      account = null,
      name = null,
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
    val newAccount = Stubs.account.copy(id = 10)
    val projectUpdater = ProjectUpdater(
      id = Stubs.project.id,
      account = Settable(newAccount),
      name = Settable("Project's Name 2"),
      type = Settable(Project.Type.COMMERCIAL),
      status = Settable(Project.Status.SUSPENDED),
    )

    val result = projectUpdater.applyTo(
      project = Stubs.project,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.project.copy(
        account = newAccount,
        name = "Project's Name 2",
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

    val projectCreator = ProjectCreator(
      account = Stubs.account,
      name = "Project's Name",
      type = Project.Type.OPENSOURCE,
      status = Project.Status.ACTIVE,
      userIdType = Project.UserIdType.USERNAME,
    )

    val projectDbm = projectCreator.toProjectData(
      timeProvider = timeProvider,
    ).apply {
      // no info about IDs from this conversion
      projectId = Stubs.projectDbm.projectId
    }

    assertThat(projectDbm).isEqualTo(Stubs.projectDbm)
  }

  @Test fun `account data to domain`() {
    val expected = Stubs.account.copy(
      // no info about this on data layer
      owners = emptyList(),
    )
    assertThat(Stubs.accountDbm.toDomain()).isDataClassEqualTo(expected)
  }

  @Test fun `account domain to data`() {
    assertThat(Stubs.account.toData()).isEqualTo(Stubs.accountDbm)
  }

  @Test fun `project data to domain`() {
    val expected = Stubs.project.copy(
      account = Stubs.account.copy(
        // no info about this on data layer
        owners = emptyList(),
      ),
    )
    assertThat(Stubs.projectDbm.toDomain()).isDataClassEqualTo(expected)
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
