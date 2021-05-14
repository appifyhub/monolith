package com.appifyhub.monolith.security

import com.appifyhub.monolith.controller.auth.UserAuthController.Endpoints as UserAuthEndpoints
import com.appifyhub.monolith.controller.common.Endpoints as CommonEndpoints
import com.appifyhub.monolith.controller.heartbeat.HeartbeatController.Endpoints as HeartbeatEndpoints
import com.appifyhub.monolith.errors.GlobalExceptionHandler
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
  private val exceptionHandler: GlobalExceptionHandler,
) : WebSecurityConfigurerAdapter() {

  public override fun configure(httpSecurityConfigurator: HttpSecurity) {
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
          UserAuthEndpoints.AUTH,
          UserAuthEndpoints.ADMIN_AUTH,
          HeartbeatEndpoints.HEARTBEAT,
          CommonEndpoints.ERROR,
          CommonEndpoints.H2_CONSOLE,
          CommonEndpoints.FAVICON,
        )
          .permitAll()
          .anyRequest()
          .authenticated()
      }
      .exceptionHandling()
      .authenticationEntryPoint(exceptionHandler)
      .disable()

      .headers()
      .frameOptions()
      .sameOrigin()
      .and()

      .oauth2ResourceServer { it.jwt() }
  }

  @Bean
  public override fun userDetailsService(): UserDetailsService = userRepository

}
