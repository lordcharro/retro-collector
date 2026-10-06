package com.retrocollector.app

import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.ComposeWindow
import org.jetbrains.compose.resources.painterResource
import com.retrocollector.app.generated.resources.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.awt.Taskbar
import javax.imageio.ImageIO

fun main() {
    System.setProperty("apple.awt.application.name", "RetroCollector")
    System.setProperty("apple.awt.application.appearance", "system")
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        System.err.println("Uncaught exception on thread ${thread.name}: ${throwable.message}")
        throwable.printStackTrace()
    }
    setupMacDockIcon()

    application {
        val windowState = rememberWindowState(
            width = 1440.dp,
            height = 900.dp,
            position = WindowPosition.Aligned(Alignment.Center)
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "Retro Collector - Language & Region Tracker (CH/EU)",
            state = windowState,
            icon = painterResource(Res.drawable.app_icon)
        ) {
            window.minimumSize = Dimension(1024, 680)
            configureMacWindow(window)
            App()
        }
    }
}

private fun setupMacDockIcon() {
    try {
        if (!Taskbar.isTaskbarSupported()) return
        val taskbar = Taskbar.getTaskbar()
        if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return

        val iconStream = Thread.currentThread().contextClassLoader.getResourceAsStream("app_icon.png") ?: return
        val img = ImageIO.read(iconStream) ?: return
        taskbar.iconImage = img
    } catch (e: Exception) {
        println("Dock icon initialization skipped: ${e.message}")
    }
}

private fun configureMacWindow(window: ComposeWindow) {
    val osName = System.getProperty("os.name")?.lowercase().orEmpty()
    if (osName.contains("mac")) {
        window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
        window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
        window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
    }
}
