package dev.kamikaze.cryptosy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.NewsItem
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun NewsCard(news: ChatPayload.News) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📰 Последние новости",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val displayItems = if (expanded) news.items else news.items.take(3)
            
            displayItems.forEachIndexed { index, newsItem ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                NewsItemRow(newsItem)
            }
            
            if (news.items.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (expanded) "Свернуть" else "Показать ещё (${news.items.size - 3})"
                    )
                }
            }
        }
    }
}

@Composable
fun NewsItemRow(item: NewsItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Open URL */ }
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.source ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTimeAgo(item.time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    
    return when {
        minutes < 1 -> "только что"
        minutes < 60 -> "$minutes мин назад"
        hours < 24 -> "$hours ч назад"
        days < 7 -> "$days дн назад"
        else -> {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}