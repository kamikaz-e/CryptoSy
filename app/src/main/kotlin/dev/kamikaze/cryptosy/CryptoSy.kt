package dev.kamikaze.cryptosy

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dev.kamikaze.cryptosy.di.AppContainer
import timber.log.Timber

class CryptoSy : Application() {

    lateinit var container: AppContainer
        private set

    companion object {
        const val CHANNEL_ID = "crypto_updates_channel"
    }

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create notification channel for foreground service
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Crypto Updates",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Real-time cryptocurrency market updates"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}