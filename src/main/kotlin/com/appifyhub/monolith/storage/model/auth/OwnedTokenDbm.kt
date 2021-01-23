package com.appifyhub.monolith.storage.model.auth

import com.appifyhub.monolith.storage.model.user.UserDbm
import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "owned_token")
class OwnedTokenDbm(

  @Id
  @Column(unique = true, nullable = false, updatable = false, length = 1024)
  var tokenLocator: String,

  @Column(nullable = false)
  var blocked: Boolean,

  @Column(nullable = true, updatable = false, length = 256)
  var origin: String?,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var expiresAt: Date,

  @ManyToOne(fetch = FetchType.EAGER)
  var owner: UserDbm,

) : Serializable
