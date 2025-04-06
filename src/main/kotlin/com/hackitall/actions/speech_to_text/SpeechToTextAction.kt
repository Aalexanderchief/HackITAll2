package com.hackitall.actions.speech_to_text

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.DialogWrapper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import java.io.File

class SpeechToTextAction : AnAction("Record Speech") {

    private var pythonProcess: Process? = null

    override fun actionPerformed(e: AnActionEvent) {
        // Create and display the dialog immediately upon clicking the action
        val dialog = RecordingDialog()
        dialog.show()  // Show dialog with the recording functionality
    }

    inner class RecordingDialog : DialogWrapper(true) {
        init {
            init()
            title = "Speech to Text Recorder"
        }

        override fun createCenterPanel(): javax.swing.JComponent {
            val panel = javax.swing.JPanel()
            val stopButton = javax.swing.JButton("Stop Recording")
            val statusLabel = javax.swing.JLabel("Recording...")  // Initial status label set to "Recording" immediately

            // Start recording immediately when the dialog is opened
            startRecording(statusLabel)

            stopButton.addActionListener {
                // Stop recording when the Stop button is clicked
                val transcribedText = getTranscription()
                Messages.showMessageDialog(
                    "You said: $transcribedText",
                    "Transcription",
                    Messages.getInformationIcon()
                )
                close(0)  // Close the dialog after displaying the transcription
            }

            panel.add(statusLabel)
            panel.add(stopButton)
            return panel
        }

        override fun doOKAction() {
            // Prevent any action when OK is pressed, as it's handled by the button logic
            super.doOKAction()
        }

        private fun startRecording(statusLabel: javax.swing.JLabel) {
            statusLabel.text = "Recording..."
            val scriptPath = "src${File.separator}main${File.separator}python${File.separator}speech_to_text${File.separator}SpeechToText.py"

            // Check if the script exists before running the process
            val scriptFile = File(scriptPath)
            if (!scriptFile.exists()) {
                statusLabel.text = "Error: Script not found"
                return
            }

            try {
                // Log start of process
                println("Starting Python process for speech-to-text...")

                // Start Python script for recording
                pythonProcess = ProcessBuilder("python3", scriptPath)
                    .start()

                // Log success of process start
                println("Python process started successfully.")
            } catch (e: IOException) {
                e.printStackTrace()
                statusLabel.text = "Error starting recording"
                println("Error starting Python process: ${e.message}")
            }
        }

        private fun getTranscription(): String {
            return try {
                // Wait for the Python process to complete
                println("Waiting for Python process to complete...")
                pythonProcess?.waitFor()

                // Log completion of the Python process
                println("Python process completed.")

                // Now that the process is finished, capture the output
                val reader = BufferedReader(InputStreamReader(pythonProcess!!.inputStream))
                val transcription = reader.readLine() ?: "Error transcribing audio"

                // Log the transcription result
                println("Transcription: $transcription")
                transcription
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error getting transcription: ${e.message}")
                "Error getting transcription"
            } finally {
                // Destroy the Python process after transcription is retrieved
                pythonProcess?.destroy()
                println("Python process destroyed.")
            }
        }
    }
}
