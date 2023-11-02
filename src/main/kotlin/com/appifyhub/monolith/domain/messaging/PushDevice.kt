package com.appifyhub.monolith.domain.messaging

import com.appifyhub.monolith.domain.user.User

data class PushDevice(
  val deviceId: String,
  val type: Type,
  val owner: User,
) {

  @Suppress("unused")
  enum class Type {
    ANDROID, IOS, WEB;

    companion object {
      fun find(name: String, default: Type? = null) =
        Type.values().firstOrNull { it.name == name }
          ?: default
          ?: throw IllegalArgumentException("Not found")
    }
  }

}
