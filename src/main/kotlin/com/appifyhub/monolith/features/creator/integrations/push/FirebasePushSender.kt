package com.appifyhub.monolith.features.creator.integrations.push

import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.util.extension.silent
import com.appifyhub.monolith.util.extension.throwPreconditionFailed
import com.appifyhub.monolith.util.extension.throwUnauthorized
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import com.google.firebase.messaging.Message as FirebaseMessage
import com.google.firebase.messaging.Notification as FirebaseNotification

@Component
class FirebasePushSender : PushSender {

  override val type = PushSender.Type.FIREBASE

  override fun send(
    project: Project,
    receiverToken: String,
    notification: PushSender.Notification?,
    data: Map<String, String>?,
  ) {
    if (project.firebaseConfig == null) throwUnauthorized { "Firebase not configured" }

    // it looks like this terrible API allows only one app instance to be created per project
    val uniqueName = "${project.name}_#${project.id}_${project.firebaseConfig.projectName}"
    val serviceAccountKey = silent {
      Base64Utils.decodeFromString(
        project.firebaseConfig.serviceAccountKeyJsonBase64,
      ).decodeToString()
    } ?: throwPreconditionFailed { "Service Account Key could not be decoded" }

    val firebase = silent { FirebaseApp.getInstance(uniqueName) }
      ?: silent(log = true) {
        FirebaseApp.initializeApp(
          FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccountKey.byteInputStream()))
            .build(),
          uniqueName, // project name must be unique
        )
      }
      ?: throwPreconditionFailed { "Failed to create a Firebase App instance" }

    // send it; explodes when wrong
    FirebaseMessaging.getInstance(firebase)
      .send(
        FirebaseMessage.builder()
          .setToken(receiverToken)
          .applyIfArgNotNull(data) { putAllData(it) }
          .applyIfArgNotNull(notification) { source ->
            setNotification(
              FirebaseNotification.builder()
                .setTitle(source.title)
                .applyIfArgNotNull(source.body) { setBody(it) }
                .applyIfArgNotNull(source.imageUrl) { setImage(it) }
                .build(),
            )
          }
          .build(),
      )
  }

  // it makes sense, trust me
  private inline fun <T, A> T.applyIfArgNotNull(
    arg: A?,
    action: T.(A) -> T,
  ): T = arg?.let {
    this.action(it)
  } ?: this

}
