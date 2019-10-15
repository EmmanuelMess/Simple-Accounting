package com.emmanuelmess.simpleaccounting.iu.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.emmanuelmess.simpleaccounting.R

val SYSTEM_CHANNEL_ID = "com.emmanuelmess.simpleaccounting.CHANNELS.SYSTEM"

fun migrationNotification(context: Context): Notification {
	createNotificationChannel(context)

	val builder = NotificationCompat.Builder(context, SYSTEM_CHANNEL_ID)
		.setSmallIcon(R.drawable.ic_sync_black_24dp)
		.setContentTitle(context.getString(R.string.migrating_notification_title))
		.setPriority(NotificationCompat.PRIORITY_LOW)
		.setAutoCancel(false)
		.setOngoing(true)
		.setProgress(0, 0, true)

	return builder.build()
}

private fun createNotificationChannel(context: Context) {
	// Create the NotificationChannel, but only on API 26+ because
	// the NotificationChannel class is new and not in the support library
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		val name = context.getString(R.string.service_channel_name)
		val importance = NotificationManager.IMPORTANCE_LOW
		val channel = NotificationChannel(SYSTEM_CHANNEL_ID, name, importance)
		// Register the channel with the system
		val notificationManager: NotificationManager = getSystemService(context, NotificationManager::class.java)!!
		notificationManager.createNotificationChannel(channel)
	}
}
