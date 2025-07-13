// src/main/kotlin/Main.kt

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    val ollamaApi = remember { OllamaApi() }
    val coroutineScope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("Why is the sky blue? Write a short answer.") }
    var responseText by remember { mutableStateOf("Response will appear here...") }
    var isLoading by remember { mutableStateOf(false) }
    // Модель, которую будем использовать. Убедитесь, что она у вас скачана.
    val modelName = "qwen2.5-coder:1.5b"

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ollama Desktop Client", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Enter your prompt for '$modelName'") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (prompt.isNotBlank()) {
                        isLoading = true
                        responseText = "" // Очищаем предыдущий ответ
                        coroutineScope.launch {
                            // 3. Вызываем исправленную функцию API
                            val result = ollamaApi.getResponse(prompt, modelName)
                            responseText = result
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Generate")
                }
            }
            Spacer(Modifier.height(16.dp))

            Divider()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(responseText, style = MaterialTheme.typography.body1)
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Prompts Desktop"
    ) {
        App()
    }
}