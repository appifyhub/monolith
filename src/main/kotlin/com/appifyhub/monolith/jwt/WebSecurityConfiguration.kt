package com.appifyhub.monolith.jwt

import com.appifyhub.monolith.controller.auth.AuthController
import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.repository.user.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class WebSecurityConfiguration(
  private val userRepository: UserRepository,
) : WebSecurityConfigurerAdapter() {

  override fun configure(httpSecurityConfigurator: HttpSecurity) {
    httpSecurityConfigurator
      .cors()
      .and()

      .csrf()
      .disable()

      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()

      .authorizeRequests {
        it.antMatchers(
          AuthController.Endpoints.AUTH,
          AuthController.Endpoints.ADMIN_AUTH,
          Endpoints.ERROR,
        )
          .permitAll()
          .anyRequest()
          .authenticated()
      }
      .exceptionHandling()
      .disable()

      .oauth2ResourceServer { it.jwt() }
  }

  @Bean
  override fun userDetailsService(): UserDetailsService = userRepository

}
