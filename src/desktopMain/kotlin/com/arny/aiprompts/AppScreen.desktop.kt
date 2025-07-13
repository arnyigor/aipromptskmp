package com.arny.aiprompts

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arny.aiprompts.di.DI
import com.arny.aiprompts.models.LlmModel
import com.arny.aiprompts.results.DataResult
import com.arny.aiprompts.ui.LlmUiState
import com.arny.aiprompts.ui.LlmViewModel

@Composable
@Preview
actual fun AppScreen() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { LlmViewModel(DI.llmInteractor, coroutineScope) }
    val state by viewModel.uiState.collectAsState()

    // DisposableEffect гарантирует, что onCleared будет вызван, когда Composable уйдет с экрана
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCleared()
        }
    }

    MaterialTheme {
        // Передаем состояние и обработчики событий в "глупый" Composable
        LlmContent(
            state = state,
            onPromptChanged = viewModel::onPromptChanged,
            onModelSelected = viewModel::onModelSelected,
            onGenerateClicked = viewModel::onGenerateClicked
        )
    }
}

@Composable
fun LlmContent(
    state: LlmUiState,
    onPromptChanged: (String) -> Unit,
    onModelSelected: (String) -> Unit,
    onGenerateClicked: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // --- ЛЕВАЯ ПАНЕЛЬ: СПИСОК МОДЕЛЕЙ ---
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(350.dp)
                .padding(16.dp)
        ) {
            Text("Select a model", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))

            when (val result = state.modelsResult) {
                is DataResult.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is DataResult.Success -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(result.data, key = { it.id }) { model ->
                            ModelItem(
                                model = model,
                                isSelected = model.isSelected,
                                onClick = { onModelSelected(model.id) }
                            )
                        }
                    }
                }

                is DataResult.Error -> {
                    Text("Error: ${result.exception?.message}", color = MaterialTheme.colors.error)
                }
            }
        }

        Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // --- ПРАВАЯ ПАНЕЛЬ: ЧАТ ---
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Send Prompt", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.prompt,
                onValueChange = onPromptChanged,
                label = { Text("Enter your prompt for '${state.selectedModel?.name ?: "no model selected"}'") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onGenerateClicked,
                enabled = !state.isGenerating && state.selectedModel != null,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Generate")
                }
            }
            Spacer(Modifier.height(16.dp))

            Divider()
            LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp)) {
                item {
                    Text(state.responseText, style = MaterialTheme.typography.body1)
                }
            }
        }
    }
}

/**
 * Вспомогательная Composable-функция для отображения одного элемента списка.
 * Остается без изменений.
 */
@Composable
fun ModelItem(model: LlmModel, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(model.name, style = MaterialTheme.typography.subtitle1)
            Text(
                text = "Context: ${model.contextLength ?: "N/A"}",
                style = MaterialTheme.typography.caption
            )
        }
    }
}
