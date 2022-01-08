package com.appifyhub.monolith.domain.messaging

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.mapper.instantiate
import com.appifyhub.monolith.util.Stubs
import java.util.Date
import org.junit.jupiter.api.Test

class TemplateDataBinderTest {

  @Test fun `user's name can bind properly`() {
    assertThat(TemplateDataBinder.User.Name.bind(Stubs.user))
      .isEqualTo(Stubs.user.name)
  }

  @Test fun `project's name can bind properly`() {
    val nameProp = Property.instantiate(ProjectProperty.NAME, Stubs.project.id, "Project Name", Date())
    assertThat(TemplateDataBinder.Project.Name.bind(listOf(nameProp)))
      .isEqualTo("Project Name")
  }

}
