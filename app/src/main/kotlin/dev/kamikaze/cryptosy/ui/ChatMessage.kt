package dev.kamikaze.cryptosy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.ChatRole
import dev.kamikaze.cryptosy.ui.components.FearGreedGauge
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatMessage(
    item: ChatItem,
    modifier: Modifier = Modifier
) {
    val isUser = item.role == ChatRole.USER
    val isSystem = item.role == ChatRole.SYSTEM

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = when {
                isSystem -> MaterialTheme.colorScheme.tertiaryContainer
                isUser -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Message content
                when (val payload = item.payload) {
                    is ChatPayload.Text -> {
                        Text(
                            text = payload.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                isSystem -> MaterialTheme.colorScheme.onTertiaryContainer
                                isUser -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }

                    is ChatPayload.Prices -> {
                        PricesCard(payload)
                    }

                    is ChatPayload.FearGreed -> {
                        FearGreedGauge(payload)
                    }

                    is ChatPayload.News -> {
                        NewsCard(payload)
                    }

                    is ChatPayload.Coin -> {
                        CoinInfoCard(payload)
                    }

                    is ChatPayload.MoonPhase -> {
                        MoonPhaseCard(payload)
                    }

                    is ChatPayload.CombinedReport -> {
                        // Combined report would show both moon and crypto data
                        Column {
                            MoonPhaseCard(payload.moonPhase)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = payload.marketSummary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Timestamp
                Text(
                    text = formatTimestamp(item.ts),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(if (isUser) Alignment.End else Alignment.Start)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
