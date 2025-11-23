package dev.kamikaze.cryptosy.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kamikaze.cryptosy.domain.model.ChatPayload

@Composable
fun MoonPhaseCard(moonPhase: ChatPayload.MoonPhase) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with emoji and phase name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Moon emoji in circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = moonPhase.phaseEmoji,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }

                    Column {
                        Text(
                            text = moonPhase.phase,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (moonPhase.waxing) "Растущая луна" else if (moonPhase.waning) "Убывающая луна" else "Нейтральная",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Moon Phase Visualization
            MoonPhaseVisualization(
                lunarAgePercent = moonPhase.lunarAgePercent,
                phase = moonPhase.phase
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Prediction section (only if cryptoPrediction exists)
            moonPhase.cryptoPrediction?.let { prediction ->
                PredictionSection(prediction)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Moon details
            MoonDetailsSection(moonPhase)
        }
    }
}

@Composable
private fun MoonPhaseVisualization(
    lunarAgePercent: Double,
    phase: String
) {
    var animatedProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(lunarAgePercent) {
        animatedProgress = (lunarAgePercent / 100f).toFloat()
    }

    val progress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000, easing = EaseInOutCubic),
        label = "moon_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(100.dp)
        ) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background circle (dark side)
            drawCircle(
                color = Color(0xFF424242),
                radius = radius,
                center = center,
                style = Fill
            )

            // Light side based on phase
            val lightWidth = radius * 2 * progress
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = lightWidth / 2,
                center = center,
                style = Fill
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${lunarAgePercent.toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = phase,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PredictionSection(prediction: dev.kamikaze.cryptosy.domain.model.CryptoPrediction) {
    val isPositive = prediction.trend.lowercase().contains("bull") ||
            prediction.trend.lowercase().contains("positive")
    val isNegative = prediction.trend.lowercase().contains("bear") ||
            prediction.trend.lowercase().contains("negative")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = when {
            isPositive -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            isNegative -> Color(0xFFF44336).copy(alpha = 0.1f)
            else -> Color(0xFFFF9800).copy(alpha = 0.1f)
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isPositive -> Icons.Default.TrendingUp
                            isNegative -> Icons.Default.TrendingDown
                            else -> Icons.Default.ArrowDownward
                        },
                        contentDescription = null,
                        tint = when {
                            isPositive -> Color(0xFF4CAF50)
                            isNegative -> Color(0xFFF44336)
                            else -> Color(0xFFFF9800)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Crypto Prediction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Confidence badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = prediction.confidence.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trend
            Text(
                text = "Trend: ${prediction.trend.uppercase()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reasoning
            Text(
                text = prediction.reasoning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recommendation
            Text(
                text = "💡 ${prediction.recommendation}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MoonDetailsSection(moonPhase: ChatPayload.MoonPhase) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🌙 Lunar Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        InfoRow("Lunar Age", "${String.format("%.1f", moonPhase.lunarAge)} days")
        InfoRow("Cycle Progress", "${moonPhase.lunarAgePercent.toInt()}%")
        InfoRow("Lunation Number", moonPhase.lunationNumber.toString())
        InfoRow("Distance", "${String.format("%.1f", moonPhase.lunarDistance)} Earth radii")

        moonPhase.lastFullMoon?.let { lastFull ->
            InfoRow("Last Full Moon", formatMoonDate(lastFull))
        }

        moonPhase.nextFullMoon?.let { nextFull ->
            InfoRow("Next Full Moon", formatMoonDate(nextFull))
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

private fun formatMoonDate(dateString: String): String {
    return try {
        val parts = dateString.split("T")[0].split("-")
        if (parts.size == 3) {
            "${parts[2]}.${parts[1]}.${parts[0]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}