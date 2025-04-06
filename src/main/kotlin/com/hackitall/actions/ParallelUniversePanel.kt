package com.hackitall.actions

import com.hackitall.actions.SymbolicExecutor.ExecutionPath
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.*

class ParallelUniversePanel : JPanel() {
    private val outputArea = JTextArea(20, 80)
    private val testArea = JTextArea(10, 80)

    private val generateButton = JButton("Generate Unit Tests")

    private val copyButton = JButton("Copy Tests to Clipboard")


    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        outputArea.isEditable = false
        testArea.isEditable = false

        add(JScrollPane(outputArea).apply { border = BorderFactory.createTitledBorder("Symbolic Execution") })
        add(generateButton)
        add(JScrollPane(testArea).apply { border = BorderFactory.createTitledBorder("Generated JUnit Tests") })
        add(copyButton)

        generateButton.addActionListener {
            val tests = ParallelUniverseSession.currentPaths.mapIndexed { i, path ->
                SymbolicExecutor.generateJUnit(path, i + 1)
            }.joinToString("\n\n")
            testArea.text = tests
        }

        copyButton.addActionListener {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(testArea.text)
            clipboard.setContents(selection, selection)
        }
    }

    fun showOutput(text: String, paths: List<ExecutionPath>) {
        outputArea.text = text
        testArea.text = ""
        ParallelUniverseSession.currentPaths = paths
    }
}

