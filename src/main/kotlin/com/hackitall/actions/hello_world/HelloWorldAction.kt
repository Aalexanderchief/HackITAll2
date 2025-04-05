package com.hackitall.actions.hello_world

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class HelloWorldAction : AnAction("Say Hello") {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showMessageDialog(
            "Hello from your custom IntelliJ plugin!",
            "Greeting",
            Messages.getInformationIcon()
        )
    }
}