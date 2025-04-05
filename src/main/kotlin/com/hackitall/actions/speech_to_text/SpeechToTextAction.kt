package com.hackitall.actions.speech_to_text

import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.DialogWrapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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
            val client = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build()

            // Prepare the audio data for Hugging Face API (using multipart form-data)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "audio.wav", audioData.toRequestBody("audio/wav".toMediaTypeOrNull()))
                .build()

            println("requestBody: $requestBody")

            // Make sure you use the correct Hugging Face API URL (replace with your desired model)
            val request = Request.Builder()
                .url("https://api-inference.huggingface.co/models/openai/whisper-large-v3-turbo")
                .header("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            println("request: $request")

            try {
                val response = client.newCall(request).execute()

                println("response: $response")

                // Check if the response was successful
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    return if (responseBody != null) {
                        // Parse the response JSON and extract the transcription
                        val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
                        val transcription = jsonResponse.get("text").asString
                        println("Transcription successful: $transcription")
                        transcription
                    } else {
                        println("Response body is null")
                        "No response body"
                    }
                } else {
                    // Print the error response body for debugging
                    val responseBody = response.body?.string()
                    println("Error response body: $responseBody")
                    return "Error transcribing audio"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return "Failed to transcribe audio"
            }
        }
    }
}
