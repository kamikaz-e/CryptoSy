package dev.kamikaze.cryptosy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kamikaze.cryptosy.domain.model.ChatPayload

@Composable
fun CoinInfoCard(coin: ChatPayload.Coin) {
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
                text = "${coin.symbol} - ${coin.name ?: "Unknown"}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            coin.price?.let { price ->
                InfoRow("Цена", "$${formatNumber(price)}")
            }
            
            coin.marketCap?.let { cap ->
                InfoRow("Капитализация", "$${formatNumber(cap)}")
            }
            
            coin.change24h?.let { change ->
                InfoRow(
                    "Изменение 24ч",
                    "${if (change > 0) "+" else ""}${String.format("%.2f", change)}%"
                )
            }
            
            coin.description?.let { desc ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatNumber(num: Double): String {
    return when {
        num >= 1_000_000_000 -> String.format("%.2fB", num / 1_000_000_000)
        num >= 1_000_000 -> String.format("%.2fM", num / 1_000_000)
        num >= 1_000 -> String.format("%,.2f", num)
        else -> String.format("%.2f", num)
    }
}