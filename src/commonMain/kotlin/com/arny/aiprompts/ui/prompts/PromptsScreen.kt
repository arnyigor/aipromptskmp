package com.arny.aiprompts.ui.prompts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arny.aiprompts.features.list.PromptListComponent
import com.arny.aiprompts.models.Prompt
import com.arny.aiprompts.models.SyncStatus
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun PromptsScreen(component: PromptListComponent) {
    val state by component.state.collectAsState()

    // Определяем лейаут один раз наверху
    BoxWithConstraints {
        val isDesktopLayout = maxWidth > 840.dp

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        if (state.isTokenDialogVisible) {
            GitHubTokenDialog(
                initialToken = state.currentGitHubToken,
                snackbarHostState = snackbarHostState,
                scope = scope,
                onDismissRequest = component::onTokenDialogDismiss,
                onSaveToken = component::onTokenSave
            )
        }

        Scaffold(
            topBar = {
                PromptsTopAppBar(
                    state = state,
                    isDesktopLayout = isDesktopLayout,
                    component = component
                )
            },
            floatingActionButton = {
                // Показываем FAB только на мобильной версии
                if (!isDesktopLayout) {
                    FloatingActionButton(onClick = component::onAddPromptClicked) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить промпт")
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            // Основной контент
            Box(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                if (isDesktopLayout) {
                    DesktopLayout(state, component)
                } else {
                    MobileLayout(state, component)
                }
            }

            // Отображение статуса синхронизации
            LaunchedEffect(state.syncStatus) {
                when (val status = state.syncStatus) {
                    SyncStatus.InProgress -> snackbarHostState.showSnackbar("Синхронизация промптов...", duration = SnackbarDuration.Indefinite)
                    is SyncStatus.Success -> snackbarHostState.showSnackbar("Синхронизация завершена. Обновлено промптов: ${status.updatedCount}", duration = SnackbarDuration.Short)
                    is SyncStatus.Error -> snackbarHostState.showSnackbar("Ошибка синхронизации: ${status.message ?: "Неизвестная ошибка"}", duration = SnackbarDuration.Long)
                    SyncStatus.None -> snackbarHostState.currentSnackbarData?.dismiss()
                    is SyncStatus.Conflicts -> snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
}

@Composable
private fun DesktopLayout(state: PromptsListState, component: PromptListComponent) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Левая панель (фильтры и список)
        MainContent(
            modifier = Modifier.weight(1f),
            state = state,
            component = component
        )
        // Правая панель (действия)
        ActionPanel(
            modifier = Modifier.width(220.dp),
            onAdd = component::onAddPromptClicked,
            onEdit = component::onEditPromptClicked,
            onDelete = component::onDeletePromptClicked,
            onSettings = component::onSettingsClicked, // Передаем обработчик настроек
            isActionEnabled = state.selectedPromptId != null
        )
    }
}

@Composable
private fun MobileLayout(state: PromptsListState, component: PromptListComponent) {
    MainContent(
        modifier = Modifier.fillMaxSize(),
        state = state,
        component = component
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromptsTopAppBar(
    state: PromptsListState,
    isDesktopLayout: Boolean,
    component: PromptListComponent
) {
    TopAppBar(
        title = {
            // Разный заголовок для разных лейаутов
            val titleText = if (isDesktopLayout) {
                "Prompt Manager - Показано ${state.currentPrompts.size} из ${state.allPrompts.size}"
            } else {
                "Prompts"
            }
            Text(titleText)
        },
        actions = {
            // Показываем меню "три точки" только на мобильной версии
            if (!isDesktopLayout) {
                Box {
                    IconButton(onClick = { component.onMoreMenuToggle(true) }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Дополнительные действия")
                    }
                    // Выпадающее меню
                    DropdownMenu(
                        expanded = state.isMoreMenuVisible,
                        onDismissRequest = { component.onMoreMenuToggle(false) }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                component.onEditPromptClicked()
                                component.onMoreMenuToggle(false)
                            },
                            enabled = state.selectedPromptId != null // Активируем, только если промпт выбран
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            onClick = {
                                component.onDeletePromptClicked()
                                component.onMoreMenuToggle(false)
                            },
                            enabled = state.selectedPromptId != null
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Настройки") },
                            onClick = {
                                component.onSettingsClicked()
                                component.onMoreMenuToggle(false)
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    state: PromptsListState,
    component: PromptListComponent
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilterPanel(state = state, component = component)

        when {
            state.isLoading && state.currentPrompts.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> ErrorState(message = state.error, onRetry = component::onRefresh)
            state.currentPrompts.isEmpty() -> EmptyState(message = "Промпты не найдены")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.currentPrompts, key = { it.id }) { prompt ->
                        PromptListItem(
                            prompt = prompt,
                            isSelected = state.selectedPromptId == prompt.id,
                            onClick = { component.onPromptClicked(prompt.id) },
                            onFavoriteClick = { component.onFavoriteClicked(prompt.id) }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PromptListItem(
    prompt: Prompt,
    isSelected: Boolean, // <-- 1. Добавляем новый параметр
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    // Вместо Card с elevation используем ElevatedCard из M3
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        // <-- 2. Управляем цветом в зависимости от того, выбран ли элемент
        colors = if (isSelected) {
            // Если выбран, используем более заметный цвет фона
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            // Иначе используем цвета по умолчанию для ElevatedCard
            CardDefaults.elevatedCardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = prompt.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = prompt.description.orEmpty().ifEmpty { "Нет описания" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (prompt.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        prompt.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { /* no-op */ },
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp)) // Добавим небольшой отступ
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (prompt.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "В избранное",
                    tint = if (prompt.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// Вспомогательные Composable для состояний
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon(AppIcons.EmptyList, ...)
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon(AppIcons.Error, ...)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}
