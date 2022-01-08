package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
@Suppress("FunctionName")
interface MessageTemplateDao : CrudRepository<MessageTemplateDbm, Long>
