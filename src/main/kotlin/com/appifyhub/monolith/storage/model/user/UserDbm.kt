package com.appifyhub.monolith.storage.model.user

import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "ah_user")
class UserDbm(

  @EmbeddedId
  var userId: UserIdDbm,

  @MapsId("projectId")
  @JoinColumn(referencedColumnName = "projectId", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.EAGER)
  val project: ProjectDbm,

  @Column(nullable = false, length = 1024)
  var signature: String,

  @Column(nullable = true, length = 64)
  var name: String?,

  @Column(nullable = false, length = 16)
  var type: String,

  @Column(nullable = false, length = 16)
  var authority: String,

  @Column(nullable = false)
  var allowsSpam: Boolean,

  @Column(nullable = true, length = 64)
  var contact: String?,

  @Column(nullable = true, length = 16)
  var contactType: String,

  @Column(nullable = true, length = 128)
  var verificationToken: String?,

  @Column(nullable = true)
  @Temporal(TemporalType.DATE)
  var birthday: Date?,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date = createdAt,

  @Embedded
  var company: OrganizationDbm?,

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  var account: AccountDbm?,

) : Serializable
