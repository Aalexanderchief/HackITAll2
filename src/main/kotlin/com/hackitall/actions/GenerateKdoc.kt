package com.hackitall.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document



class GenerateKdoc : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {


//        val project = e.project ;
//        val dialog = dialogUI(project) ;
//        dialog.show()


        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val fileText = document.text

        val functionText = findFunctionAtCaret(fileText, caretOffset) ?: return
        val functionInfo = extractFunctionInfo(functionText) ?: return
        val body = extractFunctionBody(functionText)
        val kdoc = generateKDoc(functionInfo.first, functionInfo.second, functionInfo.third, body)

        insertKDocAboveFunction(document, functionText, kdoc, e)
    }

    private fun findFunctionAtCaret(text: String, offset: Int): String? {
        val funRegex = Regex(
            """(?:\w+\s+)*fun\s+(?:<[^>]+>\s*)?\w+\s*\([^\)]*\)\s*(?::\s*[^=\{]+)?\s*(?:=|\{)""",
            RegexOption.DOT_MATCHES_ALL
        )
        val match = funRegex.findAll(text).find { offset in it.range } ?: return null
        val start = match.range.first
        var end = text.length
        var braces = 0
        for (i in match.range.last until text.length) {
            when (text[i]) {
                '{' -> braces++
                '}' -> {
                    braces--
                    if (braces == 0) {
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
            """(?:\w+\s+)*fun\s+(?:<[^>]+>\s*)?(\w+)\s*\(([^)]*)\)\s*(?::\s*([^=\{]+))?""",
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

    private fun extractFunctionBody(functionText: String): String {
        val startIndex = functionText.indexOf('{')
        if (startIndex == -1) return ""
        var braceCount = 0
        for (i in startIndex until functionText.length) {
            when (functionText[i]) {
                '{' -> braceCount++
                '}' -> {
                    braceCount--
                    if (braceCount == 0) {
                        return functionText.substring(startIndex + 1, i).trim()
                    }
                }
            }
        }
        return ""
    }

    private fun generateKDoc(
        name: String,
        parameters: List<Pair<String, String>>,
        returnType: String,
        body: String
    ): String {
        val builder = StringBuilder()
        builder.appendLine("/**")
        builder.appendLine(" * $name – auto-generated summary:")
        builder.appendLine(" *")
        val bodySummary = explainFunctionBody(body).lines().joinToString("\n * ")
        builder.appendLine(" * $bodySummary")
        builder.appendLine(" *")
        for ((p, t) in parameters) {
            builder.appendLine(" * @param $p of type $t")
        }
        if (returnType != "Unit") {
            builder.appendLine(" * @return $returnType")
        }
        val throwRegex = Regex("""throw\s+(\w+)""")
        val thrown = throwRegex.findAll(body).map { it.groupValues[1] }.toSet()
        if (thrown.isNotEmpty()) {
            for (ex in thrown) {
                builder.appendLine(" * @throws $ex TODO: reason for this exception")
            }
        } else if ("try" in body && "catch" in body) {
            builder.appendLine(" * @throws Exception TODO: possible exception from try/catch")
        }
        builder.appendLine(" */")
        return builder.toString()
    }

    private fun insertKDocAboveFunction(document: Document, functionText: String, kdoc: String, e: AnActionEvent) {
        val index = document.text.indexOf(functionText)
        if (index >= 0) {
            val project = e.project
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(index, "$kdoc\n")
            }
        }
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

    private fun explainFunctionBody(functionBody: String): String {
        val explanation = StringBuilder("This function body appears to include:")
        val checks = listOf(
            "for" to "- a for loop – iterates over a range or collection.",
            "while" to "- a while loop – executes while condition is true.",
            "if" to "- an if statement – conditional branching.",
            "else" to "- an else clause – fallback logic.",
            "when" to "- a when expression – like switch.",
            "try" to "- a try block – for exception handling.",
            "catch" to "- a catch clause – handles exceptions.",
            "throw" to "- a throw statement – throws an exception.",
            "val" to "- val declarations – immutable variables.",
            "var" to "- var declarations – mutable variables.",
            "return" to "- a return statement – exits function.",
            "map" to "- a map operation – transforms a collection.",
            "filter" to "- a filter operation – filters a collection.",
            "fun" to "- possibly a nested function or lambda.",
            "mutableListOf" to "- mutable list usage."
        )
        var found = false
        for ((keyword, desc) in checks) {
            if (functionBody.contains(keyword)) {
                explanation.appendLine("\n$desc")
                found = true
            }
        }
        if (!found) explanation.appendLine("\n- basic expressions or assignments.")
        return explanation.toString()
    }


}