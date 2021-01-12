package com.appifyhub.monolith.storage.model.admin

import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "project")
class ProjectDbm(

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(unique = true, nullable = false, updatable = false)
  var projectId: Long?,

  @ManyToOne(fetch = FetchType.EAGER)
  var account: AccountDbm,

  @Column(unique = true, nullable = false, length = 32)
  var signature: String,

  @Column(nullable = false, length = 32)
  var name: String,

  @Column(nullable = false, length = 16)
  var type: String,

  @Column(nullable = false, length = 16)
  var status: String,

  @Column(nullable = false, updatable = false, length = 16)
  var userIdType: String,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date = createdAt,

) : Serializable
