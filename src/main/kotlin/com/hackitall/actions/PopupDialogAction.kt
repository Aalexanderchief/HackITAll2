package com.hackitall.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class PopupDialogAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showMessageDialog(
            e.project,
            "Hello, this is a popup dialog!",
            "Popup Dialog",
            Messages.getInformationIcon()
        )
    }
}