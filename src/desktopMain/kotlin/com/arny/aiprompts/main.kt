import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arny.aiprompts.AppScreen

fun main(): Unit = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Prompts Desktop"
    ) {
        AppScreen()
    }
}