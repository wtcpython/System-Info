
package com.wtc.systeminfo

import android.app.ActivityManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import android.widget.RemoteViews


class MainWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // 更新每个小组件
        appWidgetIds.forEach { appWidgetId ->
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)

            // 设置小组件的点击意图
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE)
            remoteViews.setOnClickPendingIntent(R.id.widget_board, pendingIntent)

            // 更新小组件内容
            updateWidget(context, remoteViews)

            // 更新应用小组件
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun updateWidget(context: Context, remoteViews: RemoteViews) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemory = memoryInfo.totalMem.toDouble() / 1024 / 1024 / 1024
        val availableMemory = memoryInfo.availMem.toDouble() / 1024 / 1024 / 1024

        val storageStatFs = StatFs(Environment.getDataDirectory().absolutePath)
        val totalStorage = storageStatFs.totalBytes.toDouble() / 1024 / 1024 / 1024
        val availableStorage = storageStatFs.availableBytes.toDouble() / 1024 / 1024 / 1024

        remoteViews.setTextViewText(R.id.widget_total_memory, "总内存: %.2f GB".format(totalMemory))
        remoteViews.setTextViewText(R.id.widget_available_memory, "可用内存: %.2f GB".format(availableMemory))
        remoteViews.setTextViewText(R.id.widget_total_storage, "总存储: %.2f GB".format(totalStorage))
        remoteViews.setTextViewText(R.id.widget_available_storage, "可用存储: %.2f GB".format(availableStorage))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val remoteViews: RemoteViews? = null

        if (intent.action == "com.wtc.systeminfo") {
            val manger = AppWidgetManager.getInstance(context)
            val thisName = ComponentName(context, MainWidgetProvider::class.java)
            manger.updateAppWidget(thisName, remoteViews)
        }
    }
}