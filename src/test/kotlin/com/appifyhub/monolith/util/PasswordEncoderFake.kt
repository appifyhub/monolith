package com.appifyhub.monolith.util

import com.appifyhub.monolith.TestAppifyHubApplication
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Primary
@Component
@Profile(TestAppifyHubApplication.PROFILE)
class PasswordEncoderFake : PasswordEncoder {

  override fun encode(rawPassword: CharSequence?) = rawPassword?.reversed()?.toString().orEmpty()

  override fun matches(rawPassword: CharSequence?, encodedPassword: String?) = encode(rawPassword) == encodedPassword

}