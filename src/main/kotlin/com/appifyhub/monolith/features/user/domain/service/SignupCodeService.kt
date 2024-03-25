package com.appifyhub.monolith.features.user.domain.service

import com.appifyhub.monolith.features.user.domain.model.SignupCode
import com.appifyhub.monolith.features.user.domain.model.UserId
import org.springframework.transaction.annotation.Transactional

interface SignupCodeService {

  @Throws fun createCode(ownerId: UserId): SignupCode

  @Throws fun fetchAllCodesByOwner(ownerId: UserId): List<SignupCode>

  @Transactional(rollbackFor = [Exception::class])
  @Throws fun markCodeUsed(code: String, projectId: Long): SignupCode

}
