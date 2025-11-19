package dev.kamikaze.cryptosy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.CoinPrice

@Composable
fun PricesCard(prices: ChatPayload.Prices) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 Курсы (${prices.base})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            prices.coins.forEach { coin ->
                CoinRow(coin)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CoinRow(coin: CoinPrice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coin info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.displaySymbol,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = coin.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Price and change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${formatPrice(coin.price)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            coin.change1hPct?.let { change ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = if (change > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (change > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${if (change > 0) "+" else ""}${String.format("%.2f", change)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (change > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                    coin.change1hAbs?.let { abs ->
                        Text(
                            text = " (${if (abs > 0) "+" else ""}${String.format("%.2f", abs)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    return when {
        price >= 1000 -> String.format("%,.2f", price)
        price >= 1 -> String.format("%.2f", price)
        price >= 0.01 -> String.format("%.4f", price)
        else -> String.format("%.6f", price)
    }
}