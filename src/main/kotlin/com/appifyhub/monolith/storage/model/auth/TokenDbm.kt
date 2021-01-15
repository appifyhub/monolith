package com.appifyhub.monolith.storage.model.auth

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class TokenDbm(

  @Column(unique = true, nullable = false, updatable = false, length = 1024)
  var tokenLocator: String,

) : Serializable