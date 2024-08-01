package com.wtc.systeminfo

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun createNotificationChannel(context: Context) {
    val channelId = "your_channel_id"
    val channelName = "Your Channel Name"
    val channelDescription = "Your Channel Description"

    // 使用 IMPORTANCE_HIGH 以确保通知能够以悬浮通知形式显示
    val importance = NotificationManager.IMPORTANCE_HIGH

    val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
        description = channelDescription
    }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(notificationChannel)
}

@SuppressLint("MissingPermission")
fun sendNotification(context: Context) {
    val channelId = "your_channel_id"

    // 创建一个 Intent，指向目标 Activity
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    // 创建一个 PendingIntent
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 构建通知
    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentTitle("System Info")
        .setContentText("这是一条来自 System Info 的通知")
        .setPriority(NotificationCompat.PRIORITY_HIGH)  // 设置为高优先级
        .setDefaults(NotificationCompat.DEFAULT_ALL)  // 设置默认的声音、振动等
        .setAutoCancel(true)  // 点击通知后自动消失
        .setContentIntent(pendingIntent)  // 添加 PendingIntent

    with(NotificationManagerCompat.from(context)) {
        notify(1, notificationBuilder.build())
    }
}
