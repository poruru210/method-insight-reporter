/**
 * File: io/github/poruru210/methodinsight/util/Notifier.kt
 * Purpose: Wrap notification delivery so wording and group usage stay consistent.
 */
package io.github.poruru210.methodinsight.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import io.github.poruru210.methodinsight.config.PluginConfig

/**
 * Simple helper for showing IDE notifications.
 */
object Notifier {
    private fun group() = NotificationGroupManager.getInstance()
        .getNotificationGroup(PluginConfig.NOTIFICATION_GROUP_ID)

    /** Show an informational notification. */
    fun info(project: Project?, message: String) {
        group().createNotification(message, NotificationType.INFORMATION).notify(project)
    }

    /** Show an error notification. */
    fun error(project: Project?, message: String) {
        group().createNotification(message, NotificationType.ERROR).notify(project)
    }
}


