package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.User
import javax.transaction.Transactional

interface SignupCodeRepository {

  @Throws fun createCode(owner: User): SignupCode

  @Throws fun fetchAllCodesByOwner(owner: User): List<SignupCode>

  @Transactional // to prevent concurrency issues
  @Throws fun markCodeUsed(code: String): SignupCode?

}
