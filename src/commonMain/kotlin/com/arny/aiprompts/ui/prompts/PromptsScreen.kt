package com.arny.aiprompts.ui.prompts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arny.aiprompts.features.list.PromptListComponent
import com.arny.aiprompts.models.Prompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptsScreen(component: PromptListComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prompt Manager - Показано ${state.currentPrompts.size} из ${state.allPrompts.size}") },
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            val isDesktopLayout = maxWidth > 840.dp

            if (isDesktopLayout) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MainContent(
                        modifier = Modifier.weight(1f),
                        state = state,
                        component = component
                    )
                    ActionPanel(
                        modifier = Modifier.width(220.dp),
                        onAdd = component::onAddPromptClicked,
                        onEdit = component::onEditPromptClicked,
                        onDelete = component::onDeletePromptClicked,
                        isActionEnabled = state.selectedPromptId != null
                    )
                }
            } else {
                MainContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    component = component
                )
                // TODO: Для мобильной версии добавить FloatingActionButton
            }
        }
    }
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
