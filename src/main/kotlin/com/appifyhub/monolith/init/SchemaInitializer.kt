package com.appifyhub.monolith.init

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.init.SchemaInitializer.Seed.INITIAL
import com.appifyhub.monolith.repository.admin.SignatureGenerator
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.schema.SchemaService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.validation.impl.Normalizers
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

  private enum class Seed(val version: Long) { INITIAL(1L) }

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun run(args: ApplicationArguments?) {
    log.debug("Running schema initializer")

    Seed.values().forEach { seed ->
      log.debug("Checking schema '${seed.name}' (v${seed.version})")
      if (!schemaService.isInitialized(seed.version)) {
        when (seed) {
          INITIAL -> seedInitial()
        }

        val initializedSchema = Schema(version = seed.version, isInitialized = true)
        schemaService.update(initializedSchema)
      }
    }

    log.info("Schema initialization done")
  }

  /**
   * Seeds the database with initial account, project, owner, etc.
   */
  @Throws
  private fun seedInitial() {
    log.debug("Seeding initial database")

    // validate configuration
    val rootProjectName = Normalizers.ProjectName.run(rootConfig.rootProjectName)
      .requireValid { "Project Name" }
    val configuredSignature = Normalizers.RawSignatureNullified.run(rootConfig.rootOwnerSignature)
      .requireValid { "Owner Signature" }
    val ownerName = Normalizers.Name.run(rootConfig.rootOwnerName)
      .requireValid { "Owner Name" }
    val ownerEmail = Normalizers.Email.run(rootConfig.rootOwnerEmail)
      .requireValid { "Owner Email" }
    val rawOwnerSignature = configuredSignature ?: SignatureGenerator.nextSignature

    // create empty account for the root owner
    val account = adminService.addAccount()

    // create the root project
    val project = adminService.addProject(
      ProjectCreator(
        account = account,
        name = rootProjectName,
        type = Project.Type.FREE,
        status = Project.Status.ACTIVE,
        userIdType = Project.UserIdType.EMAIL,
      )
    )

    // create the owner's user in the root project
    var owner = userService.addUser(
      userIdType = project.userIdType,
      creator = UserCreator(
        userId = ownerEmail,
        projectId = project.id,
        rawSignature = rawOwnerSignature,
        name = ownerName,
        type = User.Type.ORGANIZATION,
        authority = User.Authority.OWNER,
        allowsSpam = true,
        contact = ownerEmail,
        contactType = User.ContactType.EMAIL,
        birthday = null,
        company = null,
      )
    )

    // make root user own the root account
    owner = userService.updateUser(
      userIdType = project.userIdType,
      updater = UserUpdater(
        id = owner.id,
        account = Settable(account),
        verificationToken = Settable(null),
      )
    )

    // prepare printable credentials
    val printableOwnerSignature = configuredSignature
      ?.let { "<see env.\$SEED_OWNER_SECRET>" }
      ?: rawOwnerSignature

    // print credentials
    log.info(
      """
        (see below)
        
        [[ SECRET SECTION START: PRINTED ONLY ONCE ]]
        
        Admin project '${project.name}' is now set up. 
        Project owner is '${owner.name} <${owner.contact}>'.
        
        Project ID     = ${project.id}
        User ID        = '${owner.id.userId}'
        Universal ID   = '${owner.id.toUniversalFormat()}'
        User Signature = '$printableOwnerSignature'
        
        [[ SECRET SECTION END ]]
        
      """.trimIndent()
    )
  }

}
