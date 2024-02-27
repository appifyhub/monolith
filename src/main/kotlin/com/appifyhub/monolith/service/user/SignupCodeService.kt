package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.UserId
import org.springframework.transaction.annotation.Transactional

interface SignupCodeService {

  @Throws fun createCode(ownerId: UserId): SignupCode

  @Throws fun fetchAllCodesByOwner(ownerId: UserId): List<SignupCode>

  @Transactional(rollbackFor = [Exception::class])
  @Throws fun markCodeUsed(code: String, projectId: Long): SignupCode

}
