package com.hackitall.actions.speech_to_text

import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.DialogWrapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.TargetDataLine

class SpeechToTextAction : AnAction("Record Speech") {

    private var isRecording = false
    private var audioBytes = ByteArrayOutputStream()
    private val audioFormat = AudioFormat(16000f, 16, 1, true, false)  // 16 kHz, 16-bit, mono
    private var targetDataLine: TargetDataLine? = null

    override fun actionPerformed(e: AnActionEvent) {
        // Create and display the dialog immediately upon clicking the action
        val dialog = RecordingDialog()
        dialog.show()  // Show dialog with the recording functionality
    }

    inner class RecordingDialog : DialogWrapper(true) {

        private val apiKey = "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"

        init {
            init()
            title = "Speech to Text Recorder"
        }

        override fun createCenterPanel(): javax.swing.JComponent? {
            val panel = javax.swing.JPanel()
            val stopButton = javax.swing.JButton("Stop Recording")
            val statusLabel = javax.swing.JLabel("Not Recording")  // Initial status label

            // Start recording immediately when the dialog is opened
            startRecording(statusLabel)

            stopButton.addActionListener {
                // Stop recording when the Stop button is clicked
                stopRecording()
                val transcribedText = transcribeAudio(audioBytes.toByteArray())
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
            try {
                val line: TargetDataLine = AudioSystem.getTargetDataLine(audioFormat)
                println("Attempting to open microphone...")
                line.open(audioFormat)
                line.start()
                targetDataLine = line

                // Update UI label
                statusLabel.text = "Recording..."
                println("Microphone successfully opened and recording started.")

                // Create a thread to capture audio input
                Thread {
                    val buffer = ByteArray(1024)
                    while (isRecording) {
                        val bytesRead = line.read(buffer, 0, buffer.size)
                        if (bytesRead > 0) {
                            audioBytes.write(buffer, 0, bytesRead)
                        }
                    }
                }.start()

                isRecording = true
            } catch (ex: LineUnavailableException) {
                ex.printStackTrace()
                showMessage("Error starting recording: ${ex.message}")
            }
        }

        private fun stopRecording() {
            // Stop recording
            isRecording = false
            targetDataLine?.stop()
            targetDataLine?.close()
            targetDataLine = null
            println("Recording stopped.")
        }

        private fun showMessage(message: String) {
            Messages.showMessageDialog(message, "Information", Messages.getInformationIcon())
        }

        private fun transcribeAudio(audioData: ByteArray): String {
//            val apiKey = "YOUR_OPENAI_API_KEY"  // Replace with your OpenAI API key
            val client = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build()

            val requestBody = RequestBody.create(
                "audio/wav".toMediaTypeOrNull(),
                audioData
            )

            val request = Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .header("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()

                // Check if the response was successful
                if (response.isSuccessful) {
                    // Response is OK, proceed to access the body
                    val responseBody = response.body?.string()
                    return if (responseBody != null) {
                        // Parse and return the transcription from the response
                        val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
//                        val transcription = jsonResponse.getAsJsonArray("text")[0].asString
                        val transcription = jsonResponse.get("text").asString
                        println("Transcription successful: $transcription")
                        transcription
                    } else {
                        println("Response body is null")
                        "No response body"
                    }
                } else {
                    // Response was not successful, handle the error
                    println("Error in transcription request: ${response.message}")
                    return "Error transcribing audio"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return "Failed to transcribe audio"
            }
        }
    }
}
