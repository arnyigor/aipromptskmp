package com.arny.aiprompts.ui.prompts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionPanel(
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isActionEnabled: Boolean
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Добавить промпт") }
            OutlinedButton(onClick = onEdit, enabled = isActionEnabled, modifier = Modifier.fillMaxWidth()) { Text("Редактировать") }
            OutlinedButton(onClick = {}, enabled = isActionEnabled, modifier = Modifier.fillMaxWidth()) { Text("Просмотр") }
            TextButton(
                onClick = onDelete,
                enabled = isActionEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Удалить") }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.Default.Settings, "Настройки")
            }
        }
    }
}
