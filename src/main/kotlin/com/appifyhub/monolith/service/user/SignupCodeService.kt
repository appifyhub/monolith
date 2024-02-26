package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.UserId
import javax.transaction.Transactional

interface SignupCodeService {

  @Throws fun createCode(ownerId: UserId): SignupCode

  @Throws fun fetchAllCodesByOwner(ownerId: UserId): List<SignupCode>

  @Transactional // to prevent concurrency issues
  @Throws fun markCodeUsed(code: String): SignupCode

}
