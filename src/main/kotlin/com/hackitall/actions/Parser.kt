package com.hackitall.actions

object Parser {
    data class Branch(val condition: String, val outcome: String)

    fun extractBranches(code: String): List<Branch> {
        val lines = code.lines()
        val branches = mutableListOf<Branch>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()

            if (line.isBlank() || line == "{" || line == "}") {
                i++
                continue
            }

            if (line.startsWith("if (") && line.contains(")")) {
                val condition = line.substringAfter("if (" ).substringBefore(")").trim()
                val inlineOutcome = line.substringAfter(")").trim()

                val outcomeLine = if (inlineOutcome.startsWith("return") || inlineOutcome.startsWith("throw")) {
                    inlineOutcome
                } else {
                    extractOutcome(lines, i + 1)
                }

                branches.add(Branch(condition, outcomeLine))
            } else if (line.startsWith("else if (") && line.contains(")")) {
                val condition = line.substringAfter("else if (").substringBefore(")").trim()
                val inlineOutcome = line.substringAfter(")").trim()
                val outcomeLine = if (inlineOutcome.startsWith("return") || inlineOutcome.startsWith("throw")) {
                    inlineOutcome
                } else {
                    extractOutcome(lines, i + 1)
                }
                branches.add(Branch(condition, outcomeLine))
            } else if (line.startsWith("else")) {
                val inlineOutcome = line.removePrefix("else").trim()
                val outcomeLine = if (inlineOutcome.startsWith("return") || inlineOutcome.startsWith("throw")) {
                    inlineOutcome
                } else {
                    extractOutcome(lines, i + 1)
                }
                branches.add(Branch("else", outcomeLine))
            }

            i++
        }

        return branches
    }

    private fun extractOutcome(lines: List<String>, startIndex: Int): String {
        var i = startIndex
        val body = StringBuilder()
        var braceCount = 0
        var found = false

        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("{")) braceCount++
            if (line.endsWith("}")) braceCount--

            if (line.startsWith("return") || line.startsWith("throw")) {
                body.appendLine(line)
                found = true
                if (braceCount <= 0) break
            }

            i++
        }

        return if (found) body.toString().trim() else ""
    }
}