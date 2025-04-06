package com.hackitall.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager

class ParallelUniverseDebuggerAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val document = editor.document
        val text = editor.selectionModel.selectedText ?: document.text

        val branches = Parser.extractBranches(text)
        println(branches)
        val paths = SymbolicExecutor.execute(branches)
        println(paths)

        val output = buildString {
            appendLine(">> Parallel Universe Debugger – Execution Paths:\n")
            paths.forEachIndexed { i, path ->
                val label = "Path ${i + 1}"
                val conditions =
                    if (path.constraints.isEmpty()) "(no conditions)" else path.constraints.joinToString(" AND ")
                val outcome = path.outcome
                appendLine("$label:\n  If: $conditions\n  Then: $outcome\n")
            }
        }

        ParallelUniverseToolWindow.panel?.showOutput(output, paths)

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Parallel Universe")
        toolWindow?.show()
    }
}