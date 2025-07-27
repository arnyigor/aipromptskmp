package com.arny.aiprompts.ui.prompts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.arny.aiprompts.platform.copyToClipboard
import com.arny.aiprompts.platform.openUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun GitHubTokenDialog(
    initialToken: String,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onDismissRequest: () -> Unit,
    onSaveToken: (String) -> Unit
) {
    var token by remember { mutableStateOf(initialToken) }
    var passwordVisible by remember { mutableStateOf(false) }

    val classicTokenPrefix = "github_pat_"
    val trimmedToken = token.trim()
    val isTokenValid = trimmedToken.startsWith(classicTokenPrefix) && trimmedToken.length > 30

    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
        onDismissRequest = onDismissRequest,
        title = { Text("Инструкция по созданию токена") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item { InstructionStep1() }
                    item { InstructionStep2(snackbarHostState, scope) }
                    item { InstructionStep3() }
                    item { InstructionStep4() }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Вставьте токен сюда ($classicTokenPrefix...)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = token.isNotBlank() && !isTokenValid,
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        val description = if (passwordVisible) "Скрыть токен" else "Показать токен"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )

                if (token.isNotBlank() && !isTokenValid) {
                    Text(
                        "Токен должен начинаться с 'github_pat_' и быть корректной длины",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp).fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveToken(trimmedToken) },
                enabled = isTokenValid
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun InstructionStep(number: Int, content: @Composable ColumnScope.() -> Unit) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "$number.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column(content = content)
    }
}

@Composable
private fun InstructionStep1() {
    val linkUrl = "https://github.com/settings/personal-access-tokens/new"
    InstructionStep(1) {
        val annotatedString = buildAnnotatedString {
            append("Перейдите по ссылке для ")
            pushStringAnnotation("URL", linkUrl)
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("создания токена")
            }
            pop()
            append(".")
        }
        ClickableText(annotatedString) { offset ->
            annotatedString.getStringAnnotations("URL", offset, offset)
                .firstOrNull()?.let { openUrl(it.item) }
        }
    }
}

@Composable
private fun InstructionStep2(snackbarHostState: SnackbarHostState, scope: CoroutineScope) {
    val tokenName = "AiPromptMaster-Token"
    InstructionStep(2) {
        Text("В поле \"Note\" (Название) введите:")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(tokenName, fontWeight = FontWeight.SemiBold)
            Button(onClick = {
                copyToClipboard(tokenName)
                scope.launch { snackbarHostState.showSnackbar("Название скопировано!") }
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Копировать", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Копировать")
            }
        }
    }
}

@Composable
private fun InstructionStep3() {
    InstructionStep(3) {
        Text(buildAnnotatedString {
            append("В разделе \"Select scopes\" (Выбор прав) поставьте ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("только одну")
            }
            append(" галочку напротив пункта: ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("public_repo")
            }
            append(". Это право дает доступ только к публичным репозиториям.")
        })
    }
}

@Composable
private fun InstructionStep4() {
    InstructionStep(4) {
        Text("Пролистайте вниз, нажмите \"Generate token\", скопируйте его и вставьте в поле ниже. Токен будет показан только один раз!")
    }
}