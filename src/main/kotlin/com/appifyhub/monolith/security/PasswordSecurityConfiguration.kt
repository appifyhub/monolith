package com.appifyhub.monolith.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordSecurityConfiguration {

  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

}