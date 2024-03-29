package com.appifyhub.monolith.features.user.api

import com.appifyhub.monolith.features.common.api.Endpoints
import com.appifyhub.monolith.features.auth.domain.access.AccessManager
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Privilege
import com.appifyhub.monolith.features.user.api.model.PushDeviceRequest
import com.appifyhub.monolith.features.user.api.model.PushDeviceResponse
import com.appifyhub.monolith.features.user.api.model.PushDevicesResponse
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.service.PushDeviceService
import com.appifyhub.monolith.features.common.api.model.SimpleResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PushDevicesController(
  private val pushDeviceService: PushDeviceService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.UNIVERSAL_USER_PUSH_DEVICES)
  fun addPushDevice(
    authentication: Authentication,
    @PathVariable universalId: String,
    @RequestBody pushDeviceRequest: PushDeviceRequest,
  ): PushDeviceResponse {
    log.debug("[POST] adding push device $pushDeviceRequest for user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)
    accessManager.requireProjectFeaturesFunctional(userId.projectId, Feature.PUSH)
    val user = accessManager.requestUserAccess(authentication, userId, Privilege.USER_WRITE_PUSH_DEVICE)

    return pushDeviceService.addDevice(pushDeviceRequest.toDomain(user)).toNetwork()
  }

  @GetMapping(Endpoints.UNIVERSAL_USER_PUSH_DEVICE)
  fun fetchPushDevice(
    authentication: Authentication,
    @PathVariable universalId: String,
    @PathVariable deviceId: String,
  ): PushDeviceResponse {
    log.debug("[GET] fetching push device $deviceId for user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)
    accessManager.requireProjectFeaturesFunctional(userId.projectId, Feature.PUSH)
    accessManager.requestUserAccess(authentication, userId, Privilege.USER_READ_PUSH_DEVICE)

    return pushDeviceService.fetchDeviceById(deviceId).toNetwork()
  }

  @GetMapping(Endpoints.UNIVERSAL_USER_PUSH_DEVICES)
  fun fetchAllPushDevicesForUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): PushDevicesResponse {
    log.debug("[GET] fetching all push devices for user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)
    accessManager.requireProjectFeaturesFunctional(userId.projectId, Feature.PUSH)
    val user = accessManager.requestUserAccess(authentication, userId, Privilege.USER_READ_PUSH_DEVICE)

    return pushDeviceService.fetchAllDevicesByUser(user).toNetwork()
  }

  @DeleteMapping(Endpoints.UNIVERSAL_USER_PUSH_DEVICE)
  fun removePushDevice(
    authentication: Authentication,
    @PathVariable universalId: String,
    @PathVariable deviceId: String,
  ): SimpleResponse {
    log.debug("[DELETE] deleting push device $deviceId for user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)
    accessManager.requireProjectFeaturesFunctional(userId.projectId, Feature.PUSH)
    accessManager.requestUserAccess(authentication, userId, Privilege.USER_DELETE_PUSH_DEVICE)

    pushDeviceService.deleteDeviceById(deviceId)

    return SimpleResponse.DONE
  }

  @DeleteMapping(Endpoints.UNIVERSAL_USER_PUSH_DEVICES)
  fun removeAllPushDevicesForUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): SimpleResponse {
    log.debug("[DELETE] deleting all push devices for user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)
    accessManager.requireProjectFeaturesFunctional(userId.projectId, Feature.PUSH)
    val user = accessManager.requestUserAccess(authentication, userId, Privilege.USER_DELETE_PUSH_DEVICE)

    pushDeviceService.deleteAllDevicesByUser(user)

    return SimpleResponse.DONE
  }

}
