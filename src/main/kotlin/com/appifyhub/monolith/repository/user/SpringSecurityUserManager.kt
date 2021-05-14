package com.appifyhub.monolith.repository.user

import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsPasswordService
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.UserDetailsManager

interface SpringSecurityUserManager :
  UserDetailsService,
  UserDetailsChecker,
  UserDetailsManager,
  UserDetailsPasswordService
