package org.master.git

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GitMasterAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        if (GitMasterDialog().showAndGet()) {
            GitMasterNotifier().notify("Git Master Action Clicked")
        }
    }
}
