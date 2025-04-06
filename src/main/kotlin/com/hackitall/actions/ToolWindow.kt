package com.hackitall.actions
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel

class MascotToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(BorderLayout())

        val resource = javaClass.getResource("/assets/mascot.png")

        if (resource == null) {
            println("Resource not found: /assets/mascot.png")
        }
        val icon = if (resource != null) ImageIcon(resource) else null
        val imageLabel = if (icon != null) {
            val scaledIcon = ImageIcon(icon.image.getScaledInstance(150, 200, java.awt.Image.SCALE_SMOOTH)) // Portrait type
            JLabel(scaledIcon)
        } else {
            JLabel("Image not found")
        }
        panel.add(imageLabel, BorderLayout.PAGE_END)


        val imagePaths = listOf(
            "/assets/comm1.png",
            "/assets/comm2.png",
            "/assets/comm3.png" ,
            "/assets/comm4.png",
            "/assets/comm5.png",
        )

        val imageLabels = imagePaths.map { path ->
            val resource = javaClass.getResource(path)
            if (resource == null) {
                println("Resource not found: $path")
            JLabel("Image not found")
            } else {
                val icon = ImageIcon(resource)
                val scaledIcon = ImageIcon(icon.image.getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH)) // Portrait type
                JLabel(scaledIcon)
            }
        }


        panel.add(imageLabels[0], BorderLayout.AFTER_LINE_ENDS)

        val timer = javax.swing.Timer(30000) {
            val currentLabel = panel.getComponent(1) as JLabel
            val nextIndex = (imageLabels.indexOf(currentLabel) + 1) % imageLabels.size
            panel.remove(currentLabel)
            panel.add(imageLabels[nextIndex], BorderLayout.AFTER_LINE_ENDS)
            panel.revalidate()
            panel.repaint()
        }
        timer.start()

        val content = ContentFactory.getInstance().createContent(panel, "Mascot", false)
        toolWindow.contentManager.addContent(content)
        toolWindow.show(null) // Automatically show the window
    }
}