
package com.hackitall.actions

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import com.google.gson.JsonObject

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import java.io.File


class GenerateBoilerplateCodeAction : AnAction() {
    private var transcribedText: String = ""

private fun callChatGPT(prompt: String): String {
        val client = OkHttpClient()
        val apiKey = "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" // Use env or fallback
        if (apiKey.isBlank()) return "❌ API key not set"

        val gson = Gson()

        val requestBody = gson.toJson(
            mapOf(
                "model" to "gpt-3.5-turbo",
                "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
                "temperature" to 0.7
            )
        )

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("❌ Response failed: ${response.code}")
                println(response.body?.string())
                return "Request failed: ${response.code}"
            }

            val json = response.body?.string() ?: return "Empty response"
            val root = gson.fromJson(json, JsonObject::class.java)
            return root["choices"]
                .asJsonArray[0].asJsonObject["message"]
                .asJsonObject["content"].asString
        }
    }

    private fun getTranscribedText(project: com.intellij.openapi.project.Project): String {
        try {
            val outputFile = File(project.basePath, "src/main/resources/python/output.txt")
            if (outputFile.exists()) {
                transcribedText = outputFile.readText().trim()
                return transcribedText
            }
        } catch (e: Exception) {
            Messages.showErrorDialog(project, "Error reading transcription: ${e.message}", "Error")
        }
        return ""
    }
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        // Citește textul transcris din fișierul Python
        transcribedText = getTranscribedText(project)

        if (transcribedText.isBlank()) {
            Messages.showWarningDialog(project, "Speech transcription is empty. Please try again.", "⚠️ Warning")
            return
        }

        // Creează promptul pentru ChatGPT
        val prompt = """
        🎯 You are a Kotlin boilerplate code generator.

        📝 The user will describe a Kotlin component they want (class, function, data class, etc.).  
        Your task is to generate clean, idiomatic Kotlin code based on their description.

        ✅ Rules:
        - Include necessary imports.
        - Provide full boilerplate code.
        - If unclear, generate a minimal but compilable version.
        - If the input includes a class, add a constructor and sample method.
        - If it's a function, add parameters and sample logic.
        - Format output in code block with ```kotlin.

        ❓If the input is ambiguous or too vague (e.g., "do stuff", "you know what I mean"), respond with:
        ❌ Cannot generate boilerplate. Please provide a clearer description.

        ------------------------------
        👤 User Input:
            "$transcribedText"
    """.trimIndent()

        // Send prompt and get response
        val response = callChatGPT(prompt)
        val codeBlockPattern = "```kotlin\\s*([\\s\\S]*?)```".toRegex()
        val cleanedResponse = codeBlockPattern.find(response)?.groupValues?.get(1)?.trim() ?: response.trim()

        // Insert result into editor
        val document = editor.document
        val caretOffset = editor.caretModel.offset

        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(caretOffset, "\n$cleanedResponse\n")
        }

        Messages.showInfoMessage(project, "Boilerplate code inserted based on voice input!", "✅ Success")
    }


}
