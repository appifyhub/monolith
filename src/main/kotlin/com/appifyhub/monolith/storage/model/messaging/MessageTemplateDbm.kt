package com.appifyhub.monolith.storage.model.messaging

import com.appifyhub.monolith.storage.model.creator.ProjectDbm
import java.io.Serializable
import java.util.Date
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "message_template")
class MessageTemplateDbm(

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(unique = true, nullable = false, updatable = false)
  var id: Long?,

  @JoinColumn(referencedColumnName = "projectId", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.EAGER)
  var project: ProjectDbm,

  @Column(nullable = false, length = 32, updatable = false)
  var name: String,

  @Column(nullable = false, length = 8, updatable = false)
  var language: String,

  @Column(columnDefinition = "TEXT", nullable = false)
  var content: String,

  @Column(nullable = false)
  var isHtml: Boolean,

  @OneToMany(
    targetEntity = VariableBindingDbm::class,
    fetch = FetchType.EAGER,
    cascade = [CascadeType.ALL],
    mappedBy = "template",
    orphanRemoval = true,
  )
  var bindings: List<VariableBindingDbm>,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date = createdAt,

) : Serializable {

  @Suppress("DuplicatedCode") // random warning, this isn't a duplicate
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MessageTemplateDbm) return false

    if (id != other.id) return false
    if (project.projectId != other.project.projectId) return false
    if (name != other.name) return false
    if (language != other.language) return false
    if (content != other.content) return false
    if (isHtml != other.isHtml) return false
    if (bindings != other.bindings) return false
    if (createdAt != other.createdAt) return false
    if (updatedAt != other.updatedAt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id?.hashCode() ?: 0
    result = 31 * result + project.projectId.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + language.hashCode()
    result = 31 * result + content.hashCode()
    result = 31 * result + isHtml.hashCode()
    result = 31 * result + bindings.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + updatedAt.hashCode()
    return result
  }

  override fun toString(): String {
    return "MessageTemplateDbm(" +
      "id=$id, " +
      "project.id=${project.projectId}, " +
      "name='$name', " +
      "language='$language', " +
      "content='$content', " +
      "isHtml=$isHtml, " +
      "bindings=$bindings, " +
      "createdAt=$createdAt, " +
      "updatedAt=$updatedAt" +
      ")"
  }

}
