package com.hackitall.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class HelloWorldAction : AnAction("Show Hello") {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showMessageDialog(
            e.project,
            "Salutare de la extensia ta IntelliJ!",
            "HackItAll Plugin",
            Messages.getInformationIcon()
        )
    }
}
