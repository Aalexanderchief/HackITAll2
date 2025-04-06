package com.hackitall.actions

object SymbolicExecutor {
    data class ExecutionPath(val constraints: List<String>, val outcome: String)

    fun execute(branches: List<Parser.Branch>): List<ExecutionPath> {
        val paths = mutableListOf<ExecutionPath>()

        fun walk(index: Int, constraints: List<String>) {
            if (index >= branches.size) return

            val branch = branches[index]
            val condition = branch.condition
            val outcome = branch.outcome
            val isTerminal = outcome.trim().startsWith("return") || outcome.trim().startsWith("throw")
            val isElse = condition == "else"

            val trueConstraints = if (isElse) constraints else constraints + condition
            paths.add(ExecutionPath(trueConstraints, outcome))

            if (!isElse) {
                val falseConstraints = constraints + "NOT($condition)"
                walk(index + 1, falseConstraints)
            }

            if (!isTerminal) {
                walk(index + 1, constraints)
            }
        }

        walk(0, emptyList())
        return paths
    }


    fun generateJUnit(path: ExecutionPath, index: Int): String {
        return if (path.outcome.startsWith("throw")) {
            """
            @Test
            fun testPath$index() {
                assertFailsWith<IllegalArgumentException> {
                    login(null, null)
                }
            }
            """.trimIndent()
        } else {
            val expected = path.outcome.replace("return", "").trim().removeSurrounding("\"")
            """
            @Test
            fun testPath$index() {
                val result = login("admin", "root")
                assertEquals("$expected", result)
            }
            """.trimIndent()
        }
    }

    fun suggestTestArguments(path: ExecutionPath): String {
        return when {
            "user == null" in path.constraints -> "null, \"password\""
            "password == null" in path.constraints -> "\"user\", null"
            "user == \"admin\"" in path.constraints && "password == \"root\"" in path.constraints -> "\"admin\", \"root\""
            else -> "\"user\", \"pass\""
        }
    }
}