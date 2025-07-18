# Анализ проекта AiPromptsKMP

## Технологический стек
- **Язык**: Kotlin Multiplatform (Android, Desktop)
- **UI**: Compose Multiplatform
- **Навигация**: Decompose
- **DI**: Koin + кастомный DI-контейнер
- **Сеть**: Ktor + OpenRouter API
- **Локальное хранилище**: Room/SQLite
- **Асинхронность**: Kotlin Coroutines

## Архитектура
```mermaid
graph TD
    A[UI Layer] --> B[ViewModel]
    B --> C[Интеракторы]
    C --> D[Репозитории]
    D --> E[Внешние API]
    D --> F[Локальное хранилище]
```

### Ключевые компоненты
1. **RootComponent** - корневой компонент навигации
2. **LLMInteractor** - ядро бизнес-логики
3. **OpenRouterRepository** - интеграция с LLM API
4. **PromptListComponent/PromptDetailComponent** - компоненты экранов

## Модели данных
```mermaid
classDiagram
    class Prompt {
        +String id
        +String title
        +String? description
        +PromptContent content
        +Map<String, String> variables
        +List<String> compatibleModels
        +String category
        +List<String> tags
        +Boolean isLocal
        +Boolean isFavorite
        +Float rating
        +Int ratingVotes
        +String status
        +PromptMetadata metadata
        +String version
        +Date createdAt
        +Date modifiedAt
    }

    class Chat {
        +String id
        +String name
        +Long timestamp
        +String lastMessage
    }

    class ModelDTO {
        +String id
        +String? name
        +String? description
        +Long? contextLength
        +Long? createdAt
        +ModelArchitecture? architecture
        +ModelPricing? pricing
        +toDomain() LlmModel
    }
```

## Workflow бизнес-логики
```mermaid
sequenceDiagram
    participant UI as UI Layer
    participant VM as ViewModel
    participant Interactor as LLMInteractor
    participant Repository as OpenRouterRepository
    participant API as OpenRouter API

    UI->>VM: Запрос списка моделей
    VM->>Interactor: getAvailableModels()
    Interactor->>Repository: refreshModels()
    Repository->>API: GET /api/v1/models
    API-->>Repository: Модели
    Repository-->>Interactor: Обновленные модели
    Interactor-->>VM: Список моделей
    VM-->>UI: Отображение моделей
```

## Зависимости
```mermaid
pie
    title Основные зависимости
    "Kotlin Coroutines" : 25
    "Compose Multiplatform" : 20
    "Ktor Client" : 15
    "Decompose" : 15
    "Koin" : 10
    "Room/SQLite" : 15
```

### Версии зависимостей
- **Kotlin**: 2.2.0
- **Compose**: 1.8.2
- **Ktor**: 3.1.3 (BOM)
- **Decompose**: 3.4.0-alpha03
- **Room**: 2.7.2
- **Koin**: 4.0.4

## Полная схема компонентов
```mermaid
flowchart TD
    RootComponent --> PromptListComponent
    RootComponent --> PromptDetailComponent
    PromptListComponent --> LLMInteractor
    PromptDetailComponent --> LLMInteractor
    LLMInteractor --> OpenRouterRepository
    LLMInteractor --> ChatHistoryRepository
    OpenRouterRepository --> KtorClient
```

## Особенности реализации
1. **Кроссплатформенный DI**:
   - Общие зависимости в `commonMain`
   - Платформенные реализации через expect/actual
2. **Состояние навигации**:
   - Сохранение стека через ScreenConfig.serializer()
   - Чистая архитектура компонентов
3. **Модели данных**:
   - Сериализация через kotlinx.serialization
   - Поддержка мультиязычных промптов
   - Автоматическое маппинга DTO через ModelDTO.toDomain()

## Рекомендации
1. Добавить кэширование моделей LLM
2. Реализовать IPromptSynchronizer
3. Внедрить centralized error handling
4. Добавить модульное тестирование ключевых компонентов
5. Внедрить Data Class Validation для моделей