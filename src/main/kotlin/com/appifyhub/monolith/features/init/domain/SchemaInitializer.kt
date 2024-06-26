package com.appifyhub.monolith.features.init.domain

import com.appifyhub.monolith.features.common.domain.model.Settable
import com.appifyhub.monolith.features.common.validation.Normalizers
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectCreator
import com.appifyhub.monolith.features.creator.domain.model.messaging.FirebaseConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.MailgunConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.TwilioConfig
import com.appifyhub.monolith.features.creator.domain.service.CreatorService
import com.appifyhub.monolith.features.creator.domain.service.CreatorService.Companion.DEFAULT_MAX_USERS
import com.appifyhub.monolith.features.creator.domain.service.MessageTemplateService
import com.appifyhub.monolith.features.creator.repository.SignatureGenerator
import com.appifyhub.monolith.features.init.domain.SchemaInitializer.Seed.INITIAL
import com.appifyhub.monolith.features.init.domain.model.InitializationConfig
import com.appifyhub.monolith.features.init.domain.model.Schema
import com.appifyhub.monolith.features.init.domain.service.SchemaService
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.features.user.domain.service.UserService
import com.appifyhub.monolith.util.extension.requireValid
import com.appifyhub.monolith.util.extension.silent
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class SchemaInitializer(
  private val creatorService: CreatorService,
  private val userService: UserService,
  private val schemaService: SchemaService,
  private val messageTemplateService: MessageTemplateService,
  private val initializationConfig: InitializationConfig,
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
    val configuredSignature = Normalizers.RawSignatureNullified.run(initializationConfig.ownerSignature)
      .requireValid { "Owner Signature" }
    val ownerName = Normalizers.Name.run(initializationConfig.ownerName)
      .requireValid { "Owner Name" }
    val ownerEmail = Normalizers.Email.run(initializationConfig.ownerEmail)
      .requireValid { "Owner Email" }
    val creatorProjectName = Normalizers.ProjectName.run(initializationConfig.projectName)
      .requireValid { "Project Name" }
    val rawOwnerSignature = configuredSignature ?: SignatureGenerator.nextSignature
    val mailgunConfig = silent {
      Normalizers.MailgunConfigData.run(
        MailgunConfig(
          apiKey = initializationConfig.mailgunApiKey,
          domain = initializationConfig.mailgunDomain,
          senderName = initializationConfig.mailgunSenderName,
          senderEmail = initializationConfig.mailgunSenderEmail,
        ),
      ).requireValid { "Mailgun Config" }
    }
    val twilioConfig = silent {
      Normalizers.TwilioConfigData.run(
        TwilioConfig(
          accountSid = initializationConfig.twilioAccountSid,
          authToken = initializationConfig.twilioAuthToken,
          messagingServiceId = initializationConfig.twilioMessagingServiceId,
          maxPricePerMessage = initializationConfig.twilioMaxPricePerMessage.toInt(),
          maxRetryAttempts = initializationConfig.twilioMaxRetryAttempts.toInt(),
          defaultSenderName = initializationConfig.twilioDefaultSenderName,
          defaultSenderNumber = initializationConfig.twilioDefaultSenderNumber,
        ),
      ).requireValid { "Twilio Config" }
    }
    val firebaseConfig = silent {
      Normalizers.FirebaseConfigData.run(
        FirebaseConfig(
          projectName = initializationConfig.firebaseProjectName,
          serviceAccountKeyJsonBase64 = initializationConfig.firebaseServiceAccountKeyBase64,
        ),
      ).requireValid { "Firebase Config" }
    }

    // create the root creator project
    val project = creatorService.addProject(
      projectData = ProjectCreator(
        owner = null,
        type = Project.Type.FREE,
        status = Project.Status.ACTIVE,
        userIdType = Project.UserIdType.EMAIL,
        name = creatorProjectName,
        description = null,
        logoUrl = null,
        websiteUrl = null,
        maxUsers = DEFAULT_MAX_USERS,
        anyoneCanSearch = false,
        onHold = false,
        languageTag = Locale.US.toLanguageTag(),
        requiresSignupCodes = false,
        maxSignupCodesPerUser = Integer.MAX_VALUE,
        mailgunConfig = mailgunConfig,
        twilioConfig = twilioConfig,
        firebaseConfig = firebaseConfig,
      ),
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
        signupCode = null,
      ),
    )

    // clear the verification token
    owner = userService.updateUser(
      updater = UserUpdater(
        id = owner.id,
        verificationToken = Settable(null),
      ),
    )

    // add some default templates
    messageTemplateService.initializeDefaults()

    // prepare printable credentials
    val printableOwnerSignature = configuredSignature
      ?.let { "\${env.CREATOR_OWNER_SECRET}" }
      ?: "'$rawOwnerSignature'"

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
        User Signature = $printableOwnerSignature
        
        [[ SECRET SECTION END ]]
        $margin
      """.trimIndent(),
    )
  }

}
