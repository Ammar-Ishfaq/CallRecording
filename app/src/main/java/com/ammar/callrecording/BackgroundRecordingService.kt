package com.ammar.callrecording

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat

class BackgroundRecordingService : Service() {
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var callStateListener: CallStateListener

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "RecordingChannel"
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        callStateListener = CallStateListener(this)

        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE)

        return START_STICKY
    }

    override fun onDestroy() {
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE)
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording in progress")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = CHANNEL_ID
            val channelName = "Recording Channel"
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                notificationChannel
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
