package dev.kamikaze.cryptosy.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.FearGreedValue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FearGreedGauge(data: ChatPayload.FearGreed) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "😱 ${data.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Gauge
            CircularGauge(
                value = data.now.value,
                label = data.now.valueClassification
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current value card
            CurrentValueCard(data.now)

            // Historical comparison
            if (data.yesterday != null || data.lastWeek != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Исторические данные",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.yesterday?.let { yesterday ->
                        ComparisonCard(
                            modifier = Modifier.weight(1f),
                            title = "Вчера",
                            current = data.now.value,
                            previous = yesterday.value,
                            label = yesterday.valueClassification
                        )
                    }

                    data.lastWeek?.let { lastWeek ->
                        ComparisonCard(
                            modifier = Modifier.weight(1f),
                            title = "Неделю назад",
                            current = data.now.value,
                            previous = lastWeek.value,
                            label = lastWeek.valueClassification
                        )
                    }
                }
            }

            // Update time
            data.now.updateTime?.let { updateTime ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Обновлено: ${formatUpdateTime(updateTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CircularGauge(
    value: Int,
    label: String
) {
    val animatedValue by animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "gauge_animation"
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Background arc
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to Color(0xFFE53935),    // Red
                    0.25f to Color(0xFFFB8C00),   // Orange
                    0.5f to Color(0xFFFDD835),    // Yellow
                    0.75f to Color(0xFF7CB342),   // Light Green
                    1.0f to Color(0xFF43A047)     // Green
                ),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(centerX - radius, centerY - radius)
            )

            // Indicator needle
            val angle = 135 + (270 * animatedValue)
            val angleRad = angle * PI / 180
            val needleLength = radius * 0.7f
            val needleX = centerX + (needleLength * cos(angleRad)).toFloat()
            val needleY = centerY + (needleLength * sin(angleRad)).toFloat()

            drawLine(
                color = Color.White,
                start = Offset(centerX, centerY),
                end = Offset(needleX, needleY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(animatedValue * 100).toInt()}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = getColorForValue(value)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CurrentValueCard(current: FearGreedValue) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = getColorForValue(current.value).copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getDescription(current.value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = getColorForValue(current.value)
            )
        }
    }
}

@Composable
private fun ComparisonCard(
    modifier: Modifier = Modifier,
    title: String,
    current: Int,
    previous: Int,
    label: String
) {
    val change = current - previous
    val changeIcon = when {
        change > 0 -> Icons.Default.ArrowUpward
        change < 0 -> Icons.Default.ArrowDownward
        else -> Icons.Default.Remove
    }
    val changeColor = when {
        change > 0 -> Color(0xFF43A047)
        change < 0 -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$previous",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = getColorForValue(previous)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = changeIcon,
                    contentDescription = null,
                    tint = changeColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (change > 0) "+$change" else "$change",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = changeColor
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getColorForValue(value: Int): Color = when {
    value < 25 -> Color(0xFFE53935)    // Extreme Fear - Red
    value < 45 -> Color(0xFFFB8C00)    // Fear - Orange
    value < 55 -> Color(0xFFFDD835)    // Neutral - Yellow
    value < 75 -> Color(0xFF7CB342)    // Greed - Light Green
    else -> Color(0xFF43A047)           // Extreme Greed - Green
}

private fun getDescription(value: Int): String = when {
    value < 25 -> "Крайний страх - возможность для покупки"
    value < 45 -> "Страх на рынке - осторожность инвесторов"
    value < 55 -> "Нейтральное настроение рынка"
    value < 75 -> "Жадность - рынок растёт"
    else -> "Крайняя жадность - возможная коррекция"
}

private fun formatUpdateTime(updateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(updateTime)

        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: updateTime
    } catch (e: Exception) {
        updateTime
    }
}