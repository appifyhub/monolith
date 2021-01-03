package com.appifyhub.monolith.storage.model.user

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class UserIdDbm(

  @Column(nullable = false, updatable = false, length = 128)
  var identifier: String,

  @Column(nullable = false, updatable = false)
  var projectId: Long,

) : Serializable
