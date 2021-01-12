package com.appifyhub.monolith.service.schema

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.admin.SignatureGenerator
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.schema.SchemaInitializer.SupportedVersions.INITIAL
import com.appifyhub.monolith.service.schema.SchemaInitializer.SupportedVersions.values
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.takeIfNotBlank
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SchemaInitializer(
  private val adminService: AdminService,
  private val userService: UserService,
  private val schemaService: SchemaService,
  private val rootConfig: RootProjectConfig,
) : ApplicationRunner {

  private enum class SupportedVersions { INITIAL }

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun run(args: ApplicationArguments?) {
    log.debug("Running schema initializer")

    for (supportedVersion in values()) {
      val version = supportedVersion.ordinal + 1L
      val isInitialized = schemaService.isInitialized(version)
      log.debug("Checking schema '${supportedVersion.name}' (v$version)")

      if (!isInitialized) {
        when (supportedVersion) {
          INITIAL -> seedInitial()
        }

        val updatedSchema = Schema(version = version, isInitialized = true)
        schemaService.update(updatedSchema)
      }
    }
  }

  /**
   * Seeds the database with initial account, project, owner, etc.
   */
  private fun seedInitial() {
    log.debug("Seeding initial database")

    // create empty account for the root owner
    val account = adminService.addAccount(AccountCreator(id = null))

    // create the root project
    var project = adminService.addProject(
      ProjectCreator(
        id = null,
        account = account,
        name = rootConfig.rootProjectName.takeIfNotBlank()!!,
        type = Project.Type.FREE,
        status = Project.Status.ACTIVE,
      )
    )
    if (rootConfig.rootProjectSignature.isNotBlank()) {
      project = adminService.updateProject(
        ProjectUpdater(
          id = project.id,
          rawSignature = Settable(rootConfig.rootProjectSignature),
        )
      )
    }

    // prepare the owner signature
    val rawOwnerSignature = rootConfig.rootOwnerSignature.takeIfNotBlank() ?: SignatureGenerator.nextSignature

    // create the owner's user in the root project
    var owner = userService.addUser(
      project = project,
      creator = UserCreator(
        id = null,
        projectId = project.id,
        rawSignature = rawOwnerSignature,
        name = rootConfig.rootOwnerName,
        type = User.Type.ORGANIZATION,
        authority = User.Authority.OWNER,
        allowsSpam = true,
        contact = rootConfig.rootOwnerEmail,
        contactType = User.ContactType.EMAIL,
        birthday = null,
        company = null,
      )
    )

    // make root user own the root account
    owner = userService.updateUser(
      project = project,
      updater = UserUpdater(
        id = owner.userId,
        account = Settable(account)
      )
    )

    // prepare printable credentials
    val projectSignature = rootConfig.rootProjectSignature.takeIfNotBlank()
      ?.let { "<see env.\$SEED_PROJECT_SECRET>" }
      ?: project.signature
    val ownerSignature = rootConfig.rootOwnerSignature.takeIfNotBlank()
      ?.let { "<see env.\$SEED_OWNER_SECRET>" }
      ?: rawOwnerSignature

    // print credentials
    log.info(
      """
        (see below)
        [[ THIS WILL BE PRINTED ONLY ONCE ]]
        
        Your project '${project.name}' is now set up. Your secret signature for it: '$projectSignature'.
        Account owner '${owner.name} <${owner.contact}>' with ID '${owner.userId.id}' uses
        this password to authenticate: '$ownerSignature'.
        
        [[ END OF TRANSMISSION ]]
      """.trimIndent()
    )
  }

}