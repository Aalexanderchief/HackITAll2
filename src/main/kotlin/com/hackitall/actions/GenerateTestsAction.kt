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
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import java.util.*
import com.intellij.openapi.application.ApplicationManager
import java.io.File




class GenerateTestsAction : AnAction() {
//    fun runTestClass(project: Project, testClassFqn: String) {
//        val runManager = RunManager.getInstance(project)
//
//        // Use JUnit configuration type instead of Application
//        val configurationType = JUnitConfigurationType.getInstance()
//        val configurationFactory = configurationType.configurationFactories[0]
//
//        val settings = runManager.createConfiguration(
//            "Test $testClassFqn",
//            configurationFactory
//        )
//
//        // Cast to JUnitConfiguration to set test class
//        val configuration = settings.configuration as JUnitConfiguration
//
//
//        try {
//            // Add configuration and set as selected
//            runManager.addConfiguration(settings)
//            runManager.selectedConfiguration = settings
//
//            // Execute the tests
//            ProgramRunner.getRunner(DefaultRunExecutor.EXECUTOR_ID, configuration)?.let { runner ->
//                val environment = ExecutionEnvironmentBuilder
//                    .create(DefaultRunExecutor.getRunExecutorInstance(), settings)
//                    .build()
//                runner.execute(environment)
//            }
//        } catch (e: Exception) {
//            Messages.showErrorDialog(
//                "Could not run test class: ${e.message}",
//                "Test Execution Error"
//            )
//        }
//    }

    fun runTestInTerminal(project: Project, testClass: String) {
        println(testClass)
        val testCommand = "./gradlew test --tests $testClass"

        // fallback to plain execution if TerminalView not available
        val terminalViewClass = try {
            Class.forName("com.intellij.terminal.TerminalView")
        } catch (e: Exception) {
            null
        }

        if (terminalViewClass != null) {
            val terminalView = terminalViewClass.getMethod("getInstance", Project::class.java).invoke(null, project)
            val createShellWidget = terminalViewClass.getMethod("createLocalShellWidget", String::class.java, String::class.java)
            val shell = createShellWidget.invoke(terminalView, project.basePath ?: ".", "RunTest")
            shell.javaClass.getMethod("executeCommand", String::class.java).invoke(shell, testCommand)
        } else {
            // fallback: run test in background process
            ApplicationManager.getApplication().executeOnPooledThread {
                val process = ProcessBuilder("cmd", "/c", testCommand)
                    .directory(File(project.basePath ?: "."))
                    .start()
                process.waitFor()
            }
        }
    }
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val fileText = document.text
        val selection = editor.selectionModel.selectedText ?: return

        val userPrompt = Messages.showInputDialog(
            "Please explain what is the purpose of the function:",
            "ChatGPT Prompt",
            null
        )

        if (userPrompt.isNullOrBlank()) {
            Messages.showWarningDialog("No prompt provided. Operation cancelled.", "Warning")
            return
        }

        val response = callChatGPT(
            """
    🎯 You are a Kotlin test case generator.

    ✅ Your only task is to generate **EXACTLY 5 lines** of test values based on:
    - A clear, functional description of what a Kotlin function is supposed to do.
    - The number of parameters extracted from the function's signature.

    ⚠️ VERY IMPORTANT RULE:
    If the user's message is NOT a valid description of what a function should compute (e.g. it's a question, joke, gibberish, unrelated text like "I like donuts", or just incomplete), you MUST respond with:
    
    ❌
    
    - No quotes.
    - No explanation.
    - No markdown.
    - No apology.
    - Just the ❌ character on its own line.

    🧪 If the user's input **is valid**, you must output 5 lines in the following format:
    
    param1=... param2=... result=...

    ✨ Additional rules:
    - Use realistic and diverse test values (positive/negative, strings, booleans, etc.).
    - Prefix parameters with `paramN=`, where N is their position.
    - Always end each line with the expected result using `result=...`.
    - No extra lines or text besides the 5 test case lines.

    --------------------------
    🧠 USER INPUT (natural language description of function):
    "$userPrompt"

    🧩 FUNCTION HEADER (parameter types):
    "$selection"
    """.trimIndent()
        )

        if (response.startsWith("Request failed") || response.startsWith("❌")) {
            Messages.showErrorDialog(response, "Error")
            return
        }

        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = e.project ?: return

        val currentFileName = virtualFile.nameWithoutExtension
        val testFileName = "${currentFileName}Test.kt"
        val parentDir = virtualFile.parent

        val packageName = fileText
            .lines()
            .firstOrNull { it.trim().startsWith("package ") }
            ?.removePrefix("package ")
            ?.trim()
            ?: ""

        val functionText = extractFullFunctionAtCaret(fileText, caretOffset) ?: return
        val functionInfo = extractFunctionInfo(functionText) ?: return
        val (functionName, parameters, returnType) = functionInfo
        val className = extractEnclosingClassName(fileText, caretOffset)
        val testCode = generateTestCases(
            functionName,
            parameters,
            returnType,
            className,
            response
        )

        WriteCommandAction.runWriteCommandAction(project) {
            val siblingDir = parentDir.parent?.findChild("generated-tests")
                ?: parentDir.parent?.createChildDirectory(this, "generated-tests")
                ?: return@runWriteCommandAction

            val testFile = siblingDir.findOrCreateChildData(this, testFileName)
            val existingText = if (testFile.exists()) VfsUtil.loadText(testFile) else ""
            val newContent = buildString {
                if (!existingText.contains("class ${currentFileName}Test")) {
                    if (packageName.isNotEmpty()) {
                        appendLine("package $packageName\n")
                    }
                    appendLine("import org.junit.jupiter.api.Test")
                    appendLine("import org.junit.jupiter.api.Assertions.*\n")
                    appendLine("class ${currentFileName}Test {\n")
                    append(testCode)
                    appendLine("}")
                } else {
                    val insertPos = existingText.lastIndexOf("}")
                    append(existingText.substring(0, insertPos).trimEnd())
                    appendLine()
                    append(testCode)
                    appendLine("}")
                }
            }

            testFile.setBinaryContent(newContent.toByteArray())
            val fqClassName = if (packageName.isNotEmpty()) "$packageName.${currentFileName}Test" else "${currentFileName}Test"
//            runTestClass(project, fqClassName)
            runTestInTerminal(project, fqClassName)


            val basePath = project.basePath
            if (basePath == null) {
                Messages.showErrorDialog("Project base path is null.", "Test Execution Error")
                return@runWriteCommandAction
            }
        }

        Messages.showInfoMessage(response, "ChatGPT Analysis")
    }

    fun callChatGPT(prompt: String): String {
        val client = OkHttpClient()
        val apiKey = "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" // Use env or fallback
        if (apiKey.isBlank()) return "❌ API key not set"

        val gson = Gson()

        // Build JSON properly to avoid syntax issues
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



    private fun extractFullFunctionAtCaret(text: String, offset: Int): String? {
        val headerRegex = Regex("""(?:\w+\s+)*fun\s+(?:<[^>]+>\s*)?\w+\s*\([^\)]*\)\s*(?::\s*[^=^{]+)?\s*(?:=|\{)""")
        val match = headerRegex.findAll(text).find { offset in it.range } ?: return null
        val start = match.range.first
        val bodyStart = text.indexOf('{', match.range.last)
        if (bodyStart == -1) {
            // expression-bodied function
            return text.substring(start, text.indexOf('\n', match.range.last).coerceAtLeast(match.range.last + 1))
        }
        var end = text.length
        var braceCount = 0
        for (i in bodyStart until text.length) {
            when (text[i]) {
                '{' -> braceCount++
                '}' -> {
                    braceCount--
                    if (braceCount == 0) {
                        end = i + 1
                        break
                    }
                }
            }
        }
        return text.substring(start, end)
    }

    private fun extractFunctionInfo(functionText: String): Triple<String, List<Pair<String, String>>, String>? {
        val functionPattern = Regex(
            """(?:\w+\s+)*fun\s+(?:<[^>]+>\s*)?(\w+)\s*\(([^)]*)\)\s*(?::\s*([^=^{]+))?""",
            RegexOption.DOT_MATCHES_ALL
        )
        val match = functionPattern.find(functionText) ?: return null
        val functionName = match.groupValues[1]
        val rawParams = match.groupValues[2]
        val returnType = match.groupValues.getOrNull(3)?.trim()?.ifBlank { null } ?: "Unit"

        val paramList = splitParametersSafely(rawParams).mapNotNull { param ->
            val paramRegex = Regex("""(vararg\s+)?(\w+)\s*:\s*([^=]+)""")
            val result = paramRegex.find(param.trim())
            result?.let {
                val name = it.groupValues[2]
                val type = it.groupValues[3].trim()
                name to type
            }
        }

        return Triple(functionName, paramList, returnType)
    }

    private fun generateTestCases(
        functionName: String,
        parameters: List<Pair<String, String>>,
        returnType: String,
        className: String?,
        chatGptValues: String
    ): String {
        val builder = StringBuilder()
        val testMethodName = functionName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        // 📌 Parsăm fiecare linie validă (ignorăm goale sau ❌)
        val lines = chatGptValues.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.contains("❌") && it.contains("result=") }
            .take(5)

        if (lines.isEmpty()) {
            return "// ❌ ChatGPT did not return any valid test case lines.\n"
        }

        lines.forEachIndexed { index, line ->
            val values = line.split(" ").mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap()
            val missingParams = parameters.indices.map { "param${it + 1}" }.filter { it !in values || values[it].isNullOrBlank() }
            if (missingParams.isNotEmpty() || !values.containsKey("result")) {
                builder.appendLine("    // ❌ Skipped malformed test case (missing: ${missingParams.joinToString()})")
                return@forEachIndexed
            }

            builder.appendLine("    @Test")
            builder.appendLine("    fun test$testMethodName$index() {")

            parameters.forEachIndexed { paramIndex, (name, type) ->
                val paramKey = "param${paramIndex + 1}"
                val rawValue = values[paramKey] ?: return@forEachIndexed
                val formattedValue = when (type.trim()) {
                    "String" -> "\"$rawValue\""
                    "Char" -> "'$rawValue'"
                    "Int", "Long", "Double", "Float", "Boolean" -> rawValue
                    else -> if (type.endsWith("?")) "null" else "// TODO: value for $type"
                }
                builder.appendLine("        val $name = $formattedValue")
            }

            builder.appendLine()

            if (className != null) {
                builder.appendLine("        val instance = $className()")
            }

            val call = "${if (className != null) "instance." else ""}$functionName(${parameters.joinToString(", ") { it.first }})"

            val expectedRaw = values["result"] ?: "null"
            val expectedFormatted = when (returnType.trim()) {
                "String" -> "\"$expectedRaw\""
                "Char" -> "'$expectedRaw'"
                "Int", "Long", "Double", "Float", "Boolean" -> expectedRaw
                else -> "/* TODO: expected $returnType */"
            }

            builder.appendLine("        val expected = $expectedFormatted")
            builder.appendLine("        val result = $call")
            builder.appendLine("        assertEquals(expected, result)")
            builder.appendLine("    }\n")
        }

        return builder.toString()
    }


    private fun extractEnclosingClassName(text: String, offset: Int): String? {
        val classPattern = Regex("""class\s+(\w+)\s*[{(]""")
        val matches = classPattern.findAll(text).toList()
        var lastMatchBeforeCaret: String? = null
        for (match in matches) {
            if (offset > match.range.first) {
                lastMatchBeforeCaret = match.groupValues[1]
            }
        }
        return lastMatchBeforeCaret
    }

    private fun splitParametersSafely(paramString: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var angle = 0
        var round = 0
        var square = 0
        for (char in paramString) {
            when (char) {
                '<' -> angle++
                '>' -> angle--
                '(' -> round++
                ')' -> round--
                '[' -> square++
                ']' -> square--
                ',' -> {
                    if (angle == 0 && round == 0 && square == 0) {
                        result.add(current.toString().trim())
                        current.clear()
                        continue
                    }
                }
            }
            current.append(char)
        }
        if (current.isNotBlank()) result.add(current.toString().trim())
        return result
    }
}
