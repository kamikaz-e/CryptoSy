package dev.kamikaze.cryptosy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
            // Header with icon, name, rank
            CoinHeader(coin)

            Spacer(modifier = Modifier.height(16.dp))

            // Price and main changes
            PriceSection(coin)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Market data
            MarketDataSection(coin)

            // Price changes
            if (coin.priceChange1h != null || coin.priceChange1d != null || coin.priceChange1w != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                PriceChangesSection(coin)
            }

            // Scores
            if (coin.liquidityScore != null || coin.volatilityScore != null ||
                coin.marketCapScore != null || coin.riskScore != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                ScoresSection(coin)
            }

            // Links
            if (coin.websiteUrl != null || coin.redditUrl != null || coin.twitterUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                LinksSection(coin)
            }

            // Contract addresses
            if (!coin.contractAddresses.isNullOrEmpty() || coin.contractAddress != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                ContractsSection(coin)
            }

            // Explorers
            if (!coin.explorers.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                ExplorersSection(coin)
            }

            // Description
            coin.description?.let { desc ->
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
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
private fun CoinHeader(coin: ChatPayload.Coin) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = coin.displaySymbol.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = coin.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    coin.rank?.let { rank ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#$rank",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Text(
                    text = coin.displaySymbol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PriceSection(coin: ChatPayload.Coin) {
    Column {
        coin.price?.let { price ->
            Text(
                text = "$${formatNumber(price)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        coin.priceBtc?.let { priceBtc ->
            Text(
                text = "${formatNumber(priceBtc)} BTC",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Main change indicator
        val mainChange = coin.priceChange1d ?: coin.change24h
        mainChange?.let { change ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = if (change > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (change > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (change > 0) "+" else ""}${String.format("%.2f", change)}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (change > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Text(
                    text = " (24ч)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MarketDataSection(coin: ChatPayload.Coin) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📊 Рыночные данные",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        coin.marketCap?.let { cap ->
            InfoRow("Рыночная капитализация", "$${formatLargeNumber(cap)}")
        }
        
        coin.volume?.let { vol ->
            InfoRow("Объем торгов (24ч)", "$${formatLargeNumber(vol)}")
        }
        
        coin.fullyDilutedValuation?.let { fdv ->
            InfoRow("Полная разводненная оценка", "$${formatLargeNumber(fdv)}")
        }
        
        coin.availableSupply?.let { available ->
            coin.totalSupply?.let { total ->
                InfoRow(
                    "Обращение / Всего",
                    "${formatLargeNumber(available)} / ${formatLargeNumber(total)}"
                )
            } ?: run {
                InfoRow("В обращении", formatLargeNumber(available))
            }
        }
    }
}

@Composable
private fun PriceChangesSection(coin: ChatPayload.Coin) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📈 Изменения цены",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            coin.priceChange1h?.let { change ->
                ChangeChip("1ч", change, modifier = Modifier.weight(1f))
            }
            coin.priceChange1d?.let { change ->
                ChangeChip("24ч", change, modifier = Modifier.weight(1f))
            }
            coin.priceChange1w?.let { change ->
                ChangeChip("7д", change, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChangeChip(
    label: String,
    change: Double,
    modifier: Modifier = Modifier
) {
    val color = if (change > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (change > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${if (change > 0) "+" else ""}${String.format("%.2f", change)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ScoresSection(coin: ChatPayload.Coin) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📊 Оценки",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            coin.liquidityScore?.let { score ->
                ScoreChip("Ликвидность", score, modifier = Modifier.weight(1f))
            }
            coin.volatilityScore?.let { score ->
                ScoreChip("Волатильность", score, modifier = Modifier.weight(1f))
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            coin.marketCapScore?.let { score ->
                ScoreChip("Капитализация", score, modifier = Modifier.weight(1f))
            }
            coin.riskScore?.let { score ->
                ScoreChip("Риск", score, modifier = Modifier.weight(1f))
            }
        }
        
        coin.avgChange?.let { avg ->
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow("Среднее изменение", "${String.format("%.2f", avg)}%")
        }
    }
}

@Composable
private fun ScoreChip(
    label: String,
    score: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%.1f", score),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun LinksSection(coin: ChatPayload.Coin) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🔗 Ссылки",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        coin.websiteUrl?.let { url ->
            LinkRow("Веб-сайт", url)
        }
        coin.redditUrl?.let { url ->
            LinkRow("Reddit", url)
        }
        coin.twitterUrl?.let { url ->
            LinkRow("Twitter", url)
        }
    }
}

@Composable
private fun LinkRow(label: String, url: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Open URL */ },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            maxLines = 1
        )
    }
}

@Composable
private fun ContractsSection(coin: ChatPayload.Coin) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📝 Контракты",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        coin.contractAddresses?.forEach { contract ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = contract.blockchain.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contract.contractAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { /* TODO: Copy address */ }
                    )
                }
            }
        }
        
        coin.contractAddress?.let { address ->
            if (coin.contractAddresses.isNullOrEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable { /* TODO: Copy address */ }
                    )
                }
            }
        }
        
        coin.decimals?.let { decimals ->
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow("Десятичные знаки", decimals.toString())
        }
    }
}

@Composable
private fun ExplorersSection(coin: ChatPayload.Coin) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🔍 Обозреватели блокчейна",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        coin.explorers?.forEach { explorer ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Open URL */ },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = explorer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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

private fun formatLargeNumber(num: Double): String {
    return when {
        num >= 1_000_000_000_000 -> String.format("%.2fT", num / 1_000_000_000_000)
        num >= 1_000_000_000 -> String.format("%.2fB", num / 1_000_000_000)
        num >= 1_000_000 -> String.format("%.2fM", num / 1_000_000)
        num >= 1_000 -> String.format("%.2fK", num / 1_000)
        else -> String.format("%.2f", num)
    }
}
