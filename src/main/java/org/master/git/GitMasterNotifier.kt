package org.master.git

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project


class GitMasterNotifier {
    private val group = NotificationGroup("Git Master Opinions", NotificationDisplayType.BALLOON, true)
    fun notify(content: String?): Notification {
        return notify(null, content)
    }

    private fun notify(project: Project?, content: String?): Notification {
        val notification: Notification = group.createNotification(content!!, NotificationType.INFORMATION)
        notification.notify(project)
        return notification
    }
}