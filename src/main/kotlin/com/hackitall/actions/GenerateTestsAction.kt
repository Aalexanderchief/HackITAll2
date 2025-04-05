package com.hackitall.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class GenerateTestsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) as? KtFile ?: return

        val classesOrObjects = file.declarations.filterIsInstance<KtClassOrObject>()
        val topLevelFunctions = file.declarations.filterIsInstance<KtNamedFunction>()

        val virtualFile = file.virtualFile ?: return
        val testClassName = virtualFile.nameWithoutExtension + "Test"
        val testFile = PsiFileFactory.getInstance(project).createFileFromText(
            "$testClassName.kt",
            KotlinLanguage.INSTANCE,
            generateTestFileContent(classesOrObjects, topLevelFunctions)
        )

        val directory = file.containingDirectory ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            directory.add(testFile)
        }
    }
    private fun KtClassOrObject.isObject(): Boolean {
        return this::class.simpleName == "KtObjectDeclaration"
    }

    private fun generateTestFileContent(
        classesOrObjects: List<KtClassOrObject>,
        topLevelFunctions: List<KtNamedFunction>
    ): String {
        val classBasedTests = classesOrObjects.flatMap { ktClassOrObject ->
            val className = ktClassOrObject.name ?: return@flatMap emptyList<String>()
            val isObject = ktClassOrObject.isObject()

            ktClassOrObject.declarations.filterIsInstance<KtNamedFunction>().map { function ->
                val callPrefix = if (isObject) "$className." else "$className()."
                """
                @Test
                fun ${function.name}Test() {
                    // TODO: Test pentru ${function.name}
                    val result = ${callPrefix}${function.name}()
                    println(result) // sau assertEquals(...)
                }
                """.trimIndent()
            }
        }

        val topLevelTests = topLevelFunctions.map { function ->
            """
            // Test pentru funcție top-level
            @Test
            fun ${function.name}Test() {
                // TODO: Test pentru ${function.name}
                val result = ${function.name}()
                println(result) // sau assertEquals(...)
            }
            """.trimIndent()
        }

        val allTests = (classBasedTests + topLevelTests).joinToString("\n\n")

        return """
        import org.junit.Test
        import org.junit.Assert.*

        class GeneratedTest {

        $allTests

        }
        """.trimIndent()
    }
}
