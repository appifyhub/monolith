package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.messaging.PushDevice
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.network.messaging.PushDeviceResponse
import com.appifyhub.monolith.network.messaging.PushDevicesResponse
import com.appifyhub.monolith.network.messaging.ops.PushDeviceRequest

fun PushDevice.toNetwork() = PushDeviceResponse(
  deviceId = deviceId,
  type = type.name,
)

fun Collection<PushDevice>.toNetwork() = PushDevicesResponse(
  devices = map(PushDevice::toNetwork),
)

fun PushDeviceRequest.toDomain(owner: User) = PushDevice(
  deviceId = deviceId,
  type = PushDevice.Type.find(type),
  owner = owner,
)
