package com.appifyhub.monolith.service.integrations.email

import com.appifyhub.monolith.domain.creator.Project
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Component
class LogEmailSender : EmailSender {

  data class SentEmail(
    val projectId: Long,
    val toEmail: String,
    val title: String,
    val body: String,
    val isHtml: Boolean,
  )

  private val log = LoggerFactory.getLogger(this::class.java)

  override val type = EmailSender.Type.LOG

  val history: ArrayDeque<SentEmail> by limitedDeque(limit = 50)

  override fun send(
    project: Project,
    toEmail: String,
    title: String,
    body: String,
    isHtml: Boolean,
  ) = SentEmail(project.id, toEmail, title, body, isHtml).let {
    history += it
    log.info("Email sent: $it")
  }

}

/**
 * Produces a delegated property of ArrayDeque type.
 * The custom property holds the collection internally and limits it to a maximum of 'limit' items.
 * Limiting/trimming happens on each read and write of the property.
 *
 * @param limit How big can the collection grow before it gets trimmed
 * @return Kotlin's delegated property
 * @sample LogEmailSender.history
 */
@Suppress("SameParameterValue")
private fun <E> limitedDeque(limit: Int): ReadWriteProperty<Any, ArrayDeque<E>> =
  object : ReadWriteProperty<Any, ArrayDeque<E>> {

    private var deque: ArrayDeque<E> = ArrayDeque(limit)

    override fun getValue(thisRef: Any, property: KProperty<*>): ArrayDeque<E> {
      applyLimit()
      return deque
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: ArrayDeque<E>) {
      deque = value
      applyLimit()
    }

    private fun applyLimit() {
      while (deque.size > limit) {
        deque.removeFirst()
      }
    }

  }
