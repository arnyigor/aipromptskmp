import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import org.nirmato.ollama.api.ChatRequest.Companion.chatRequest
import org.nirmato.ollama.api.Message
import org.nirmato.ollama.api.Role
import org.nirmato.ollama.client.ktor.OllamaClient

class OllamaApi {

    // 1. Используем правильный класс клиента из библиотеки nirmato.
    // Конструктор по умолчанию уже настроен на http://localhost:11434
    val ollamaClient = OllamaClient(CIO) {
        httpClient {
            // ktor HttpClient configurations
            defaultRequest {
                url("http://localhost:11434/api/")
            }
        }
    }


    /**
     * Отправляет запрос к модели и возвращает ответ.
     * @param prompt Текст запроса от пользователя.
     * @param modelName Имя модели для запроса.
     * @return Строка с ответом от модели или сообщение об ошибке.
     */
    suspend fun getResponse(prompt: String, modelName: String): String {
        return try {
            println("Sending prompt to Ollama: $prompt")
            val request = chatRequest {
                model(modelName)
                // 2. ИСПРАВЛЕНО: Используем `prompt` из параметра, а не захардкоженный текст.
                messages(listOf(Message(role = Role.USER, content = prompt)))
            }

            val response = ollamaClient.chat(request)
            response.message?.content ?: "No content in response."
        } catch (e: Exception) {
            println("Error while communicating with Ollama: ${e.message}")
            e.printStackTrace() // Печатаем полный стектрейс для отладки
            "Error: ${e.message}"
        }
    }
}