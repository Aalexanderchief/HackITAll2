package com.hackitall.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


class dialogUI(project: Project?) : DialogWrapper(project) {

    init {
        title = "PNG Preview"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val label = JLabel("PNG Preview")
        panel.add(label, BorderLayout.NORTH)

        val pngPath = "/home/rares-hampi/Desktop/HackITAll2/src/main/kotlin/com/hackitall/actions/assets/mascot.png"
        val image = renderPNGToImage(pngPath)

        if (image != null) {
            val icon = ImageIcon(image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))
            val imageLabel = JLabel(icon)
            panel.add(imageLabel, BorderLayout.CENTER)
        } else {
            panel.add(JLabel("❗ Could not load PNG"), BorderLayout.CENTER)
        }

        return panel

    }

    fun renderPNGToImage(path: String): BufferedImage? {
        return try {
            ImageIO.read(File(path))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}