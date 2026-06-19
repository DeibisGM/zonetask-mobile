package com.app.zonetask.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.zonetask.MainActivity
import com.app.zonetask.R
import com.app.zonetask.core.UserMessages
import com.app.zonetask.navigation.AppDestinations
import kotlin.math.absoluteValue

object ZoneTaskNotificationManager {

    private const val CHANNEL_ID = "zonetask_task_events"
    private const val EXTRA_SPACE_ID = "extra_space_id"
    private const val EXTRA_TASK_ID = "extra_task_id"
    private const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (manager.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            UserMessages.Notifications.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = UserMessages.Notifications.CHANNEL_DESCRIPTION
        }

        manager.createNotificationChannel(channel)
    }

    fun showTaskNotification(
        context: Context,
        title: String,
        body: String,
        spaceId: Int,
        taskId: Int,
        notificationType: String
    ) {
        // Build a local notification that deep-links into the task detail route.
        ensureChannel(context)

        val route = AppDestinations.taskDetailRoute(spaceId, taskId)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_SPACE_ID, spaceId)
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_NOTIFICATION_TYPE, notificationType)
            putExtra("notification_route", route)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            route.hashCode().absoluteValue,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        NotificationManagerCompat.from(context).notify(route.hashCode().absoluteValue, notification)
    }

    fun extractRoute(intent: Intent?): String? {
        if (intent == null) return null

        // Prefer the explicit task ids, but keep a fallback string for older taps.
        val spaceId = intent.getIntExtra(EXTRA_SPACE_ID, -1)
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        if (spaceId <= 0 || taskId <= 0) {
            return intent.getStringExtra("notification_route")
        }

        return AppDestinations.taskDetailRoute(spaceId, taskId)
    }
}
