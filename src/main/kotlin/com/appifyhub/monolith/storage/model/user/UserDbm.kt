package com.appifyhub.monolith.storage.model.user

import com.appifyhub.monolith.storage.model.creator.ProjectDbm
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
  var id: UserIdDbm,

  @MapsId("projectId")
  @JoinColumn(referencedColumnName = "projectId", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.EAGER)
  var project: ProjectDbm,

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
  @Temporal(TemporalType.TIMESTAMP)
  var birthday: Date?,

  @Embedded
  var company: OrganizationDbm?,

  @Column(nullable = true, length = 8)
  var languageTag: String?,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date = createdAt,

) : Serializable {

  @Suppress("DuplicatedCode") // false positive
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is UserDbm) return false

    if (id != other.id) return false
    if (project != other.project) return false
    if (signature != other.signature) return false
    if (name != other.name) return false
    if (type != other.type) return false
    if (authority != other.authority) return false
    if (allowsSpam != other.allowsSpam) return false
    if (contact != other.contact) return false
    if (contactType != other.contactType) return false
    if (verificationToken != other.verificationToken) return false
    if (birthday != other.birthday) return false
    if (company != other.company) return false
    if (languageTag != other.languageTag) return false
    if (createdAt != other.createdAt) return false
    if (updatedAt != other.updatedAt) return false

    return true
  }

  @Suppress("DuplicatedCode") // false positive
  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + project.hashCode()
    result = 31 * result + signature.hashCode()
    result = 31 * result + (name?.hashCode() ?: 0)
    result = 31 * result + type.hashCode()
    result = 31 * result + authority.hashCode()
    result = 31 * result + allowsSpam.hashCode()
    result = 31 * result + (contact?.hashCode() ?: 0)
    result = 31 * result + contactType.hashCode()
    result = 31 * result + (verificationToken?.hashCode() ?: 0)
    result = 31 * result + (birthday?.hashCode() ?: 0)
    result = 31 * result + (company?.hashCode() ?: 0)
    result = 31 * result + (languageTag?.hashCode() ?: 0)
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + updatedAt.hashCode()
    return result
  }

  @Suppress("DuplicatedCode") // false positive
  override fun toString(): String {
    return "UserDbm(" +
      "id=$id, " +
      "project=$project, " +
      "signature='$signature', " +
      "name=$name, " +
      "type='$type', " +
      "authority='$authority', " +
      "allowsSpam=$allowsSpam, " +
      "contact=$contact, " +
      "contactType='$contactType', " +
      "verificationToken=$verificationToken, " +
      "birthday=$birthday, " +
      "company=$company, " +
      "languageTag=$languageTag, " +
      "createdAt=$createdAt, " +
      "updatedAt=$updatedAt " +
      ")"
  }

}
