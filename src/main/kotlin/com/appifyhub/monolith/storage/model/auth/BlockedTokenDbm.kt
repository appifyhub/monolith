package com.appifyhub.monolith.storage.model.auth

import com.appifyhub.monolith.storage.model.user.UserDbm
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.ManyToOne

@IdClass(TokenDbm::class)
@Entity(name = "blocked_token")
class BlockedTokenDbm(

  @Id
  @Column(unique = true, nullable = false, updatable = false, length = 1024)
  var token: String,

  @ManyToOne(fetch = FetchType.EAGER)
  var owner: UserDbm,

) : Serializable
