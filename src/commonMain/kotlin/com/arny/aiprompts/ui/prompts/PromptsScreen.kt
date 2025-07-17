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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arny.aiprompts.features.list.PromptListComponent
import com.arny.aiprompts.models.Prompt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PromptsScreen(component: PromptListComponent) {
    // Используем расширение от Decompose для подписки на StateFlow
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Промпты") },
                // Тут можно добавить цвета, если нужно кастомизировать
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Добавить") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Добавить промпт") },
                onClick = {}
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Фильтры (как в моем первом ответе)
            FilterChips(
                selectedFilter = state.selectedFilter,
                onFilterChange = component::onFilterChanged,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Убираем Box и pull-to-refresh для простоты, можно вернуть позже
            when {
                state.isLoading && state.currentPrompts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    ErrorState(
                        message = state.error.orEmpty(),
                        onRetry = component::onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                state.currentPrompts.isEmpty() -> {
                    EmptyState(
                        message = "Промпты не найдены",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.currentPrompts, key = { it.id }) { prompt ->
                            PromptListItem(
                                prompt = prompt,
                                onClick = { component.onPromptClicked(prompt.id) },
                                onFavoriteClick = { component.onFavoriteClicked(prompt.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptListItem(
    prompt: Prompt,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    // Вместо Card с elevation используем ElevatedCard из M3
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = prompt.title,
                    style = MaterialTheme.typography.titleMedium, // Вместо h6
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = prompt.description.orEmpty().ifEmpty { "Нет описания" },
                    style = MaterialTheme.typography.bodyMedium, // Вместо body2
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Вместо кастомного Chip используем FlowRow и SuggestionChip
                FlowRow {
                    prompt.tags.forEach { tag ->
                        SuggestionChip(
                            onClick = { /* no-op */ },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (prompt.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "В избранное",
                    // Правильная работа с цветами в M3
                    tint = if (prompt.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FilterChips(
    selectedFilter: PromptsListState.Filter,
    onFilterChange: (PromptsListState.Filter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == PromptsListState.Filter.ALL,
            onClick = { onFilterChange(PromptsListState.Filter.ALL) },
            label = { Text("Все") }
        )
        FilterChip(
            selected = selectedFilter == PromptsListState.Filter.FAVORITES,
            onClick = { onFilterChange(PromptsListState.Filter.FAVORITES) },
            label = { Text("Избранные") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )
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
