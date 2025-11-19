package dev.kamikaze.cryptosy.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.kamikaze.cryptosy.ChatViewModel
import dev.kamikaze.cryptosy.ui.utils.ToolsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Auto-scroll to bottom on new messages
    LaunchedEffect(uiState.items.size) {
        if (uiState.items.isNotEmpty()) {
            listState.animateScrollToItem(uiState.items.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crypto Chat") },
                actions = {
                    // Service toggle button
                    IconButton(
                        onClick = {
                            if (uiState.isServiceRunning) {
                                viewModel.onEvent(ChatEvent.StopService)
                            } else {
                                viewModel.onEvent(ChatEvent.StartService)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.isServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isServiceRunning) "Stop updates" else "Start updates",
                            tint = if (uiState.isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Tools button
                    IconButton(onClick = { viewModel.onEvent(ChatEvent.ToolsPressed) }) {
                        Icon(Icons.Default.Build, contentDescription = "Tools")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { _, _ ->
                            focusManager.clearFocus()
                        }
                    },
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.items,
                    key = { it.id }
                ) { item ->
                    ChatMessage(item)
                }
            }

            // Input panel
            ChatInputPanel(
                text = uiState.input,
                onTextChanged = { viewModel.onEvent(ChatEvent.InputChanged(it)) },
                onSend = { viewModel.onEvent(ChatEvent.SendPressed) },
                isSending = uiState.isSending
            )
        }
    }

    // Tools dialog
    if (uiState.showToolsDialog) {
        ToolsDialog(
            tools = uiState.tools,
            onToolSelected = { viewModel.onEvent(ChatEvent.ToolPicked(it)) },
            onDismiss = { viewModel.onEvent(ChatEvent.ToolsDialogDismissed) }
        )
    }

    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar
        }
    }
}

@Composable
fun ChatInputPanel(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 200.dp),
                placeholder = { Text("Введите сообщение...") },
                maxLines = 10,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (!isSending) onSend() }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = onSend,
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}