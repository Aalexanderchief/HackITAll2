package com.hackitall.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ParallelUniverseToolWindow : ToolWindowFactory {
    companion object {
        var panel: ParallelUniversePanel? = null
    }


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val ui = ParallelUniversePanel()
        panel = ui
        val content = ContentFactory.getInstance().createContent(ui, "", false)
        toolWindow.contentManager.addContent(content)
    }
}