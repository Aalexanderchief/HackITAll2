package com.hackitall.actions.speech_to_text

import java.io.IOException

class testVoice {
    fun startVoiceRecording() {
        val pythonScriptPath = "/path/to/your/script.py"
        val processBuilder = ProcessBuilder("python3", pythonScriptPath)
        try {
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                println("Python script executed successfully.")
            } else {
                println("Python script execution failed with exit code $exitCode.")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}