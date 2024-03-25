package com.appifyhub.monolith.features.creator.domain.service

import com.appifyhub.monolith.domain.common.mapValueNonNull
import com.appifyhub.monolith.domain.common.mapValueNullable
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.eventbus.EventPublisher
import com.appifyhub.monolith.eventbus.ProjectCreated
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectCreator
import com.appifyhub.monolith.features.creator.domain.model.ProjectUpdater
import com.appifyhub.monolith.features.creator.repository.CreatorRepository
import com.appifyhub.monolith.util.extension.requireValid
import com.appifyhub.monolith.util.extension.silent
import com.appifyhub.monolith.util.extension.throwLocked
import com.appifyhub.monolith.util.extension.throwNotFound
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CreatorServiceImpl(
  private val creatorRepository: CreatorRepository,
  private val eventPublisher: EventPublisher,
) : CreatorService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addProject(projectData: ProjectCreator): Project {
    log.debug("Adding project $projectData")

    // hack it for the root creator project (this service itself)
    val creatorProject = silent(log = false) { getCreatorProject() }
    if (creatorProject != null) {
      // root creator project is already created
      if (projectData.owner == null) error("Project creator must be provided")

      if (projectData.owner.id.projectId != creatorProject.id) {
        throwLocked { "Projects can be added only by creator project users" }
      }
    } else {
      // looks like we're setting up the root creator project
      if (projectData.owner != null) error("Project's future owner must not be provided for creator project")
    }

    val normalizedName = Normalizers.ProjectName.run(projectData.name)
      .requireValid { "Project's Name" }
    val normalizedDescription = Normalizers.ProjectDescription.run(projectData.description)
      .requireValid { "Project's Description" }
    val normalizedLogoUrl = Normalizers.ProjectLogoUrl.run(projectData.logoUrl)
      .requireValid { "Project's Logo URL" }
    val normalizedWebsiteUrl = Normalizers.ProjectWebsiteUrl.run(projectData.websiteUrl)
      .requireValid { "Project's Website URL" }
    val normalizedMaxUsers = Normalizers.Cardinal.run(projectData.maxUsers.toLong())
      .requireValid { "Project's Max Users" }.toInt()
    val normalizedAnyoneCanSearch = Normalizers.FlagDefFalse.run(projectData.anyoneCanSearch)
      .requireValid { "Project's Anyone Can Search" }
    val normalizedOnHold = Normalizers.FlagDefTrue.run(projectData.onHold)
      .requireValid { "Project's On Hold" }
    val normalizedLanguageTag = Normalizers.LanguageTag.run(projectData.languageTag)
      .requireValid { "Project's Language Tag" }
    val normalizedRequiresSignupCodes = Normalizers.FlagDefFalse.run(projectData.requiresSignupCodes)
      .requireValid { "Project's Requires Signup Codes" }
    val normalizedMaxSignupCodesPerUser = Normalizers.Cardinal.run(projectData.maxSignupCodesPerUser.toLong())
      .requireValid { "Project's Max Signup Codes Per User" }.toInt()
    val normalizedMailgunConfig = Normalizers.MailgunConfigData.run(projectData.mailgunConfig)
      .requireValid { "Mailgun Config" }
    val normalizedTwilioConfig = Normalizers.TwilioConfigData.run(projectData.twilioConfig)
      .requireValid { "Twilio Config" }
    val normalizedFirebaseConfig = Normalizers.FirebaseConfigData.run(projectData.firebaseConfig)
      .requireValid { "Firebase Config" }

    val normalizedProjectCreator = ProjectCreator(
      owner = projectData.owner,
      type = projectData.type,
      status = projectData.status,
      userIdType = projectData.userIdType,
      name = normalizedName,
      description = normalizedDescription,
      logoUrl = normalizedLogoUrl,
      websiteUrl = normalizedWebsiteUrl,
      maxUsers = normalizedMaxUsers,
      anyoneCanSearch = normalizedAnyoneCanSearch,
      onHold = normalizedOnHold,
      languageTag = normalizedLanguageTag,
      requiresSignupCodes = normalizedRequiresSignupCodes,
      maxSignupCodesPerUser = normalizedMaxSignupCodesPerUser,
      mailgunConfig = normalizedMailgunConfig,
      twilioConfig = normalizedTwilioConfig,
      firebaseConfig = normalizedFirebaseConfig,
    )

    return creatorRepository.addProject(normalizedProjectCreator)
      .also { result ->
        creatorProject?.let {
          eventPublisher.publish(ProjectCreated(ownerProject = it, payload = result))
        }
      }
  }

  override fun fetchAllProjects(): List<Project> {
    log.debug("Getting all creator projects")
    return creatorRepository.fetchAllProjects()
  }

  override fun getCreatorProject(): Project {
    log.debug("Getting creator project")
    return creatorRepository.getCreatorProject()
  }

  override fun getSuperCreator(): User {
    log.debug("Getting creator owner")
    return creatorRepository.getSuperCreator()
  }

  override fun fetchProjectById(id: Long): Project {
    log.debug("Fetching project by id $id")
    val normalizedProjectId = Normalizers.ProjectId.run(id).requireValid { "Project ID" }
    return creatorRepository.fetchProjectById(normalizedProjectId)
  }

  override fun fetchAllProjectsByCreator(creator: User): List<Project> {
    log.debug("Fetching all projects for creator $creator")

    if (creator.id.projectId != getCreatorProject().id) {
      throwNotFound { "Non-service creators don't have any projects" }
    }

    return creatorRepository.fetchAllProjectsByCreatorUserId(creator.id)
  }

  override fun fetchProjectCreator(projectId: Long): User {
    log.debug("Fetching project creator for $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    return creatorRepository.fetchProjectCreator(normalizedProjectId)
  }

  override fun updateProject(updater: ProjectUpdater): Project {
    log.debug("Updating project $updater")

    val normalizedProjectId = Normalizers.ProjectId.run(updater.id).requireValid { "Project ID" }

    val normalizedName = updater.name?.mapValueNonNull {
      Normalizers.ProjectName.run(it).requireValid { "Project's Name" }
    }
    val normalizedDescription = updater.description?.mapValueNullable {
      Normalizers.ProjectDescription.run(it).requireValid { "Project's Description" }
    }
    val normalizedLogoUrl = updater.logoUrl?.mapValueNullable {
      Normalizers.ProjectLogoUrl.run(it).requireValid { "Project's Logo URL" }
    }
    val normalizedWebsiteUrl = updater.websiteUrl?.mapValueNullable {
      Normalizers.ProjectWebsiteUrl.run(it).requireValid { "Project's Website URL" }
    }
    val normalizedMaxUsers = updater.maxUsers?.mapValueNonNull {
      Normalizers.Cardinal.run(it.toLong()).requireValid { "Project's Max Users" }.toInt()
    }
    val normalizedAnyoneCanSearch = updater.anyoneCanSearch?.mapValueNonNull {
      Normalizers.FlagDefFalse.run(it).requireValid { "Project's Anyone Can Search" }
    }
    val normalizedOnHold = updater.onHold?.mapValueNonNull {
      Normalizers.FlagDefTrue.run(it).requireValid { "Project's On Hold" }
    }
    val normalizedLanguageTag = updater.languageTag?.mapValueNullable {
      Normalizers.LanguageTag.run(it).requireValid { "Language Tag" }
    }
    val normalizedRequiresSignupCodes = updater.requiresSignupCodes?.mapValueNonNull {
      Normalizers.FlagDefFalse.run(it).requireValid { "Project's Requires Signup Codes" }
    }
    val normalizedMaxSignupCodesPerUser = updater.maxSignupCodesPerUser?.mapValueNonNull {
      Normalizers.Cardinal.run(it.toLong()).requireValid { "Project's Max Signup Codes Per User" }.toInt()
    }
    val normalizedMailgunConfig = updater.mailgunConfig?.mapValueNullable {
      Normalizers.MailgunConfigData.run(it).requireValid { "Mailgun Config" }
    }
    val normalizedTwilioConfig = updater.twilioConfig?.mapValueNullable {
      Normalizers.TwilioConfigData.run(it).requireValid { "Twilio Config" }
    }
    val normalizedFirebaseConfig = updater.firebaseConfig?.mapValueNullable {
      Normalizers.FirebaseConfigData.run(it).requireValid { "Firebase Config" }
    }

    val normalizedUpdater = ProjectUpdater(
      id = normalizedProjectId,
      type = updater.type,
      status = updater.status,
      name = normalizedName,
      description = normalizedDescription,
      logoUrl = normalizedLogoUrl,
      websiteUrl = normalizedWebsiteUrl,
      maxUsers = normalizedMaxUsers,
      anyoneCanSearch = normalizedAnyoneCanSearch,
      onHold = normalizedOnHold,
      languageTag = normalizedLanguageTag,
      requiresSignupCodes = normalizedRequiresSignupCodes,
      maxSignupCodesPerUser = normalizedMaxSignupCodesPerUser,
      mailgunConfig = normalizedMailgunConfig,
      twilioConfig = normalizedTwilioConfig,
      firebaseConfig = normalizedFirebaseConfig,
    )

    return creatorRepository.updateProject(normalizedUpdater)
  }

  override fun removeProjectById(projectId: Long) {
    log.debug("Removing project $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    // we can't delete the creator project
    if (getCreatorProject().id == projectId) {
      throwLocked { "Creator project can't be deleted" }
    }

    return creatorRepository.removeProjectById(normalizedProjectId)
  }

  override fun removeAllProjectsByCreator(creatorId: UserId) {
    log.debug("Removing all projects for creator $creatorId")

    // we can't delete the creator project
    if (creatorId.projectId != getCreatorProject().id) {
      throwLocked { "Projects can be removed only by creator project users" }
    }

    creatorRepository.removeAllProjectsByCreator(creatorId)
  }

}
