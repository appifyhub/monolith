package com.appifyhub.monolith.security

import com.appifyhub.monolith.controller.auth.UserAuthController
import com.appifyhub.monolith.controller.heartbeat.HeartbeatController
import com.appifyhub.monolith.repository.user.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

import com.appifyhub.monolith.controller.common.Endpoints as CommonEndpoints

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
          UserAuthController.Endpoints.AUTH,
          UserAuthController.Endpoints.ADMIN_AUTH,
          HeartbeatController.Endpoints.HEARTBEAT,
          CommonEndpoints.ERROR,
          CommonEndpoints.H2_CONSOLE,
          CommonEndpoints.FAVICON,
        )
          .permitAll()
          .anyRequest()
          .authenticated()
      }
      .exceptionHandling()
      .disable()

      .headers()
      .frameOptions()
      .sameOrigin()
      .and()

      .oauth2ResourceServer { it.jwt() }
  }

  @Bean
  override fun userDetailsService(): UserDetailsService = userRepository

}
