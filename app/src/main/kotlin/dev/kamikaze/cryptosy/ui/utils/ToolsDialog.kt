package dev.kamikaze.cryptosy.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.kamikaze.cryptosy.domain.model.ToolItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsDialog(
    tools: List<ToolItem>,
    onToolSelected: (ToolItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredTools = remember(tools, searchQuery) {
        if (searchQuery.isBlank()) {
            tools
        } else {
            tools.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛠️ Доступные инструменты",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Поиск инструментов...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tools list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTools) { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = { onToolSelected(tool) }
                        )
                    }
                    
                    if (filteredTools.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Инструменты не найдены",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = getIconForTool(tool.id) + " " + tool.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "\"${tool.sampleQuery}\"",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

private fun getIconForTool(toolId: String): String = when (toolId) {
    "pricesSummary" -> "💰"
    "fearGreed" -> "😱"
    "news" -> "📰"
    "coinInfo" -> "ℹ️"
    else -> "🔧"
}