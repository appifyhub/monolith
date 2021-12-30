package com.appifyhub.monolith.init

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.init.SchemaInitializer.Seed.INITIAL
import com.appifyhub.monolith.repository.creator.SignatureGenerator
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.creator.PropertyService
import com.appifyhub.monolith.service.schema.SchemaService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.validation.impl.Normalizers
import java.util.Locale
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SchemaInitializer(
  private val creatorService: CreatorService,
  private val userService: UserService,
  private val propertyService: PropertyService,
  private val schemaService: SchemaService,
  private val creatorConfig: CreatorProjectConfig,
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
    val configuredSignature = Normalizers.RawSignatureNullified.run(creatorConfig.ownerSignature)
      .requireValid { "Owner Signature" }
    val ownerName = Normalizers.Name.run(creatorConfig.ownerName)
      .requireValid { "Owner Name" }
    val ownerEmail = Normalizers.Email.run(creatorConfig.ownerEmail)
      .requireValid { "Owner Email" }
    val creatorProjectName = Normalizers.PropProjectName.run(creatorConfig.projectName)
      .requireValid { "Project Name" }
    val rawOwnerSignature = configuredSignature ?: SignatureGenerator.nextSignature

    // create the creator project
    val project = creatorService.addProject(
      projectInfo = ProjectCreator(
        owner = null,
        type = Project.Type.FREE,
        status = Project.Status.ACTIVE,
        userIdType = Project.UserIdType.EMAIL,
      ),
    )

    // save the project name
    propertyService.saveProperty<String>(
      projectId = project.id,
      propName = ProjectProperty.NAME.name,
      propRawValue = creatorProjectName,
    )

    // set that the project is not on hold
    propertyService.saveProperty<Boolean>(
      projectId = project.id,
      propName = ProjectProperty.ON_HOLD.name,
      propRawValue = false.toString(),
    )

    // create the owner's user in the creator project
    var owner = userService.addUser(
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
        languageTag = Locale.US.toLanguageTag(),
      )
    )

    // clear the verification token
    owner = userService.updateUser(
      updater = UserUpdater(
        id = owner.id,
        verificationToken = Settable(null),
      )
    )

    // prepare printable credentials
    val printableOwnerSignature = configuredSignature
      ?.let { "<see \$env.CREATOR_OWNER_SECRET>" }
      ?: rawOwnerSignature

    // print credentials
    val margin = "\n".repeat(8)
    log.info(
      """
        (see below)
        $margin
        
        [[ SECRET SECTION START: PRINTED ONLY ONCE ]]
        
        Creator project '$creatorProjectName' is now set up. 
        Project owner is '${owner.name} <${owner.contact}>'.
        
        Project ID     = ${project.id}
        User ID        = '${owner.id.userId}'
        Universal ID   = '${owner.id.toUniversalFormat()}'
        User Signature = '$printableOwnerSignature'
        
        [[ SECRET SECTION END ]]
        $margin
      """.trimIndent()
    )
  }

}
