package dev.kamikaze.cryptosy.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dev.kamikaze.cryptosy.CryptoSy
import dev.kamikaze.cryptosy.MainActivity
import dev.kamikaze.cryptosy.R
import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.ChatRole
import dev.kamikaze.cryptosy.domain.usecase.GetMoonPhaseUseCase
import dev.kamikaze.cryptosy.domain.usecase.GetSummaryUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CryptoUpdateService : Service() {

    private lateinit var getSummaryUseCase: GetSummaryUseCase
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var updateJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val UPDATE_INTERVAL_MS = 60_000L // 1 minute

        private val _summaryFlow = MutableSharedFlow<List<ChatItem>>()
        val summaryFlow = _summaryFlow.asSharedFlow()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("CryptoUpdateService created")

        val app = application as CryptoSy
        getSummaryUseCase = app.container.getSummaryUseCase
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("CryptoUpdateService started")

        // Start foreground with notification
        val notification = createNotification("Отслеживание рынка активно")
        startForeground(NOTIFICATION_ID, notification)

        // Start periodic updates
        startPeriodicUpdates()

        return START_STICKY
    }

    private fun startPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                try {
                    fetchSummary()
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching summary")
                }
                delay(UPDATE_INTERVAL_MS * 2)
            }
        }
    }

    private fun fetchSummary() {
        serviceScope.launch {
            Timber.d("Fetching periodic summary (crypto + moon)...")

        getSummaryUseCase()
            .onSuccess { items ->
                    Timber.d("Summary fetched: ${items.size} items")
                    _summaryFlow.emit(items)
                    updateNotification("Last update: ${formatTime(System.currentTimeMillis())}")
                }
            .onFailure { error ->
                Timber.e(error, "Failed to fetch summary")
                    updateNotification("Update failed: ${error.message}")
                }
        }
    }

    private fun createNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CryptoSy.CHANNEL_ID)
            .setContentTitle("Crypto Sy")
            .setContentText(text)
            .setSmallIcon(R.drawable.notification_24)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(timeMillis: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timeMillis))
    }

    override fun onDestroy() {
        Timber.d("CryptoUpdateService destroyed")
        updateJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}