package com.appifyhub.monolith.features.auth.domain.security

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.errors.GlobalExceptionHandler
import com.appifyhub.monolith.repository.user.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
@EnableWebSecurity
class WebSecurityConfiguration(
  private val userRepository: UserRepository,
  private val exceptionHandler: GlobalExceptionHandler,
) : WebSecurityConfigurerAdapter() {

  public override fun configure(httpSecurityConfigurator: HttpSecurity) {
    httpSecurityConfigurator
      // enable CORS
      .cors()
      .and()
      // disable CSRF
      .csrf()
      .disable()
      // set up session management
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      // set up no-auth endpoints
      .authorizeRequests { urlRegistry ->
        urlRegistry.antMatchers(
          Endpoints.ERROR,
          Endpoints.FAVICON,
          Endpoints.FAVICON_DIR,
          Endpoints.HEARTBEAT,
          Endpoints.H2_CONSOLE,
          Endpoints.USER_AUTH,
          Endpoints.SIGNUP_USERS,
          Endpoints.SIGNUP_CREATORS,
          Endpoints.CREATOR_AUTH,
          Endpoints.VERIFICATION,
          Endpoints.SIGNATURE_RESET,
          Endpoints.DOCS,
        )
          .permitAll()
          .anyRequest()
          .authenticated()
      }
      // enable smart error handling
      .exceptionHandling()
      .authenticationEntryPoint(exceptionHandler)
      .accessDeniedHandler(exceptionHandler)
      .and()
      // enable other basic HTTP features
      .headers()
      .frameOptions()
      .sameOrigin()
      .and()
      // enable JWT server
      .oauth2ResourceServer { configurer -> configurer.jwt() }
  }

  @Bean
  public override fun userDetailsService(): UserDetailsService = userRepository

}
