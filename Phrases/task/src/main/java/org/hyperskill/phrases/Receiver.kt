package org.hyperskill.phrases

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class Receiver : BroadcastReceiver() {

    private val CHANNEL_ID = "org.hyperskill.phrases"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {

        val application = context.applicationContext as PhraseApplication

        val newIntent = Intent(context, MainActivity::class.java)
        val pIntent =
            PendingIntent.getActivity(context, 1, newIntent, PendingIntent.FLAG_IMMUTABLE)

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your phrase of the day")
            .setContentText(application.database.getPhraseDao().getAll().random().text)
            .setStyle(NotificationCompat.BigTextStyle())
            .setAutoCancel(true)
            .setContentIntent(pIntent)

        mNotificationManager.notify(393939, notificationBuilder.build())
    }
}

