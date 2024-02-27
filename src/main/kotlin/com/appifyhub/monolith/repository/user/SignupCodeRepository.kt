package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.User

interface SignupCodeRepository {

  @Throws fun createCode(owner: User): SignupCode

  @Throws fun fetchSignupCodeById(code: String): SignupCode

  @Throws fun fetchAllSignupCodesByOwner(owner: User): List<SignupCode>

  @Throws fun saveSignupCode(signupCode: SignupCode): SignupCode

}
