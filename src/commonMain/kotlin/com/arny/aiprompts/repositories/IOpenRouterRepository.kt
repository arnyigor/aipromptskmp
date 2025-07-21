import com.arny.aiprompts.models.ChatCompletionResponse
import com.arny.aiprompts.models.LlmModel
import com.arny.aiprompts.models.ChatMessage
import kotlinx.coroutines.flow.Flow

interface IOpenRouterRepository {
    /**
     * Предоставляет реактивный поток со списком всех доступных моделей.
     * Этот поток можно слушать, чтобы получать обновления.
     */
    fun getModelsFlow(): Flow<List<LlmModel>>

    /**
     * Запускает принудительное обновление списка моделей из сети.
     * Возвращает Result, чтобы можно было отследить успех/ошибку операции.
     */
    suspend fun refreshModels(): Result<Unit>

    /**
     * Выполняет запрос к API чата.
     */
    suspend fun getChatCompletion(
        model: String,
        messages: List<ChatMessage>,
        apiKey: String,
    ): Result<ChatCompletionResponse>
}
