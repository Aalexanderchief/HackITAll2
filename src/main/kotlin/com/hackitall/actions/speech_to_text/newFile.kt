package com.hackitall.actions.speech_to_text

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import io.ktor.utils.io.errors.*
import java.io.ByteArrayInputStream
import java.io.File
import javax.sound.sampled.*

class Speech : AnAction("Record and Save Audio") {

    private val defaultAudioFormat = AudioFormat(16000f, 16, 1, true, false)
    private val recordingDurationMillis: Long = 5000 // Record for 5 seconds

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val saveDirectory = project?.basePath ?: System.getProperty("user.home")
        val saveFilePath = "$saveDirectory/recorded_audio_${System.currentTimeMillis()}.wav"

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Recording and Saving Audio...", true) {
            var recordingSuccessful = false

            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Recording audio..."
                recordingSuccessful = recordAndSaveAudio(recordingDurationMillis, saveFilePath, defaultAudioFormat)
            }

            override fun onSuccess() {
                if (recordingSuccessful) {
                    Messages.showInfoMessage("Audio saved to: $saveFilePath", "Recording Successful")
                    // You can now proceed with transcription or other actions using this saved file
                } else {
                    // Message would have been shown by recordAndSaveAudio on failure
                }
            }

            override fun onThrowable(error: Throwable) {
                Messages.showErrorDialog(project, "Error during recording: ${error.message}", "Recording Error")
            }
        })
    }

    private fun recordAndSaveAudio(durationMillis: Long, saveFilePath: String, audioFormat: AudioFormat): Boolean {
        var targetDataLine: TargetDataLine? = null
        var success = false
        try {
            val line: TargetDataLine = AudioSystem.getTargetDataLine(audioFormat)
            line.open(audioFormat)
            line.start()
            targetDataLine = line

            val buffer = ByteArray(1024)
            val startTime = System.currentTimeMillis()
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()

            while (System.currentTimeMillis() - startTime < durationMillis) {
                val bytesRead = line.read(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                }
            }

            val audioData = byteArrayOutputStream.toByteArray()
            if (audioData.isNotEmpty()) {
                val inputStream = AudioInputStream(ByteArrayInputStream(audioData), audioFormat, audioData.size.toLong() / audioFormat.frameSize)
                val outputFile = File(saveFilePath)
                AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, outputFile)
                println("Audio saved to: ${outputFile.absolutePath}")
                success = true
            } else {
                println("No audio data recorded.")
            }

        } catch (ex: LineUnavailableException) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showErrorDialog("Microphone not available: ${ex.message}", "Recording Error")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            ApplicationManager.getApplication().invokeLater {
                Messages.showErrorDialog("Error saving audio file to $saveFilePath: ${e.message}", "Save Error")
            }
        } finally {
            targetDataLine?.stop()
            targetDataLine?.close()
        }
        return success
    }
}