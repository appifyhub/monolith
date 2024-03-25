package com.appifyhub.monolith.features.creator.domain.service

object MessageTemplateDefaults {

  object ProjectCreated {
    const val NAME = "project-created"
    const val TITLE = "Your new project"
    const val CONTENT = "Hey {{user.name}}, '{{project.name}}' was just created."
  }

  object UserCreated {
    const val NAME = "user-created"
    const val TITLE = "Welcome!"
    const val CONTENT = "Hey {{user.name}}, your account was just created.\n\nVerification code: {{user.code}}."
  }

  object UserAuthResetCompleted {
    const val NAME = "user-auth-reset-completed"
    const val TITLE = "Authentication changed"
    const val CONTENT = "Hey {{user.name}}, your new authentication is {{user.signature}}."
  }

}
