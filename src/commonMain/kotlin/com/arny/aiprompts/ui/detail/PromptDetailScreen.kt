package com.arny.aiprompts.ui.detail

import androidx.compose.runtime.Composable
import com.arny.aiprompts.features.details.PromptDetailComponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalClipboardManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptDetailScreen(component: PromptDetailComponent) {
    val state by component.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.prompt?.title ?: "Загрузка...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = component::onBackClicked) {
                        // Иконки из icons-extended кросс-платформенные
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    state.prompt?.let { prompt ->
                        IconButton(onClick = { component.onFavoriteClicked() }) {
                            Icon(
                                imageVector = if (prompt.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "В избранное",
                                tint = if (prompt.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Здесь можно использовать LazyColumn, как я предлагал, или Column + verticalScroll, как у вас.
        // LazyColumn эффективнее, если секций может быть много.
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading -> item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                state.error != null -> item { ErrorState(state.error.orEmpty(), component::onRefresh) }
                state.prompt != null -> {
                    val prompt = state.prompt

                    val ru = prompt?.content?.ru
                    if (!ru.isNullOrBlank()) {
                        item {
                            PromptContentCard(
                                language = "Русский",
                                text = ru,
                                onCopyClick = { clipboardManager.setText(AnnotatedString(ru)) }
                            )
                        }
                    }
                    val en = prompt?.content?.en
                    if (!en.isNullOrBlank()) {
                        item {
                            PromptContentCard(
                                language = "English",
                                text = en,
                                onCopyClick = { clipboardManager.setText(AnnotatedString(en)) }
                            )
                        }
                    }
                    val tags = prompt?.tags
                    if (!tags.isNullOrEmpty()) {
                        item { TagsSection("Теги", tags) }
                    }
                    val compatibleModels = prompt?.compatibleModels
                    if (!compatibleModels.isNullOrEmpty()) {
                        item { TagsSection("Совместимые модели", compatibleModels) }
                    }
                }
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

@Composable
private fun PromptContentCard(
    language: String,
    text: String,
    onCopyClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(language, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onCopyClick) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Копировать $language")
                }
            }
            Spacer(Modifier.height(8.dp))
            SelectionContainer { // Позволяет выделять и копировать текст
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(title: String, tags: List<String>) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            tags.forEach { tag ->
                SuggestionChip(
                    onClick = { /* Можно реализовать действие по клику */ },
                    label = { Text(tag) }
                )
            }
        }
    }
}
