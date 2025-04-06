package com.hackitall.actions.speech_to_text

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.File

import com.intellij.openapi.ui.Messages
class SpeechToTextAction : AnAction("Record Speech") {

    private fun runCommand(command: List<String>, workingDir: File? = null, project: com.intellij.openapi.project.Project) {
        try {
            val process = ProcessBuilder(command)
                .apply { if (workingDir != null) directory(workingDir) }
                .redirectErrorStream(true)
                .start()

            val outputFile = File(project.basePath, "src/main/resources/python/output.txt")
            outputFile.parentFile.mkdirs() // Ensure directory exists
            outputFile.writeText("") // Clear previous content

            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    println("🐍 Python says: $line")
                    outputFile.appendText("$line\n") // Write each line immediately
                }
            }

            val exitCode = process.waitFor()
            println("✅ Command exited with code $exitCode")

        } catch (e: Exception) {
            println("❌ Error running command: ${e.message}")
        }
    }
private fun displayTranscribedMessage(project: com.intellij.openapi.project.Project) {
    try {
        val outputFile = File(project.basePath, "src/main/resources/python/output.txt")
        if (outputFile.exists()) {
            val message = outputFile.readText().trim()
            Messages.showInfoMessage(project, message, "Transcribed Message")
        } else {
            Messages.showErrorDialog(project, "No transcription found.", "Error")
        }
    } catch (e: Exception) {
        Messages.showErrorDialog(project, "Error reading transcription: ${e.message}", "Error")
    }
}
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val scriptStream = this::class.java.getResourceAsStream("/python/SpeechToText.py")
        if (scriptStream == null) {
            println("❌ Could not find Python script in resources")
            return
        }

        val tempDir = kotlin.io.path.createTempDirectory("speech").toFile()
        val scriptFile = File(tempDir, "SpeechToText.py")

        scriptFile.outputStream().use { outStream ->
            scriptStream.copyTo(outStream)
        }

        val pythonPath = File(project.basePath, "src/main/resources/python")
        runCommand(listOf("pip", "install", "SpeechRecognition", "pyaudio"), null, project)
        runCommand(listOf("python", scriptFile.absolutePath), pythonPath, project)
        displayTranscribedMessage(project)
    }
}
