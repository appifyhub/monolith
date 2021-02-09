package com.appifyhub.monolith.domain.mapper

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.Date

class SpringUserMapperTest {

  @Test fun `user domain to spring`() {
    assertThat(Stubs.user.toSecurityUser()).all {
      prop("username") { it.username }
        .isEqualTo(Stubs.userId.toUnifiedFormat())
      prop("password") { it.password }
        .isEqualTo(Stubs.user.signature)
      prop("authorities") { it.authorities.toSet() }
        .isEqualTo(setOf(Authority.DEFAULT, Authority.MODERATOR, Authority.ADMIN))
      prop("isAccountNonLocked") { it.isAccountNonLocked }
        .isEqualTo(false)
      prop("isEnabled") { it.isEnabled }
        .isEqualTo(false)
      prop("isCredentialsNonExpired") { it.isCredentialsNonExpired }
        .isEqualTo(true)
    }
  }

  @Test fun `user spring to domain`() {
    val springUser = mock<UserDetails> {
      on { username } doReturn Stubs.userId.toUnifiedFormat()
      on { password } doReturn "drowssap"
      on { authorities } doReturn listOf(GrantedAuthority { "ADMIN" })
      on { isAccountNonLocked } doReturn true
    }
    val timeProvider = TimeProviderFake(staticTime = 10L)

    assertThat(springUser.toDomain(timeProvider)).isDataClassEqualTo(
      Stubs.user.copy(
        name = null,
        type = User.Type.PERSONAL,
        authority = Authority.ADMIN,
        allowsSpam = false,
        contact = null,
        contactType = User.ContactType.CUSTOM,
        verificationToken = null,
        birthday = null,
        createdAt = Date(10L),
        updatedAt = Date(10L),
        company = null,
        ownedTokens = emptyList(),
        account = null,
      )
    )
  }

}