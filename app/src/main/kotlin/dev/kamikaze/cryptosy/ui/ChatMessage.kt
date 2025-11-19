package dev.kamikaze.cryptosy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.ChatRole
import dev.kamikaze.cryptosy.ui.components.FearGreedGauge
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatMessage(item: ChatItem) {
    val isUser = item.role == ChatRole.USER
    val isSystem = item.role == ChatRole.SYSTEM

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Timestamp
        Text(
            text = formatTimestamp(item.ts),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        // Message content
        when (item.payload) {
            is ChatPayload.Text -> TextMessageBubble(item.payload.text, isUser, isSystem)
            is ChatPayload.Prices -> PricesCard(item.payload)
            is ChatPayload.FearGreed -> FearGreedGauge(item.payload)
            is ChatPayload.News -> NewsCard(item.payload)
            is ChatPayload.Coin -> CoinInfoCard(item.payload)
        }
    }
}

@Composable
fun TextMessageBubble(text: String, isUser: Boolean, isSystem: Boolean) {
    Box(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                )
            )
            .background(
                when {
                    isSystem -> MaterialTheme.colorScheme.tertiaryContainer
                    isUser -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isSystem -> MaterialTheme.colorScheme.onTertiaryContainer
                isUser -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}