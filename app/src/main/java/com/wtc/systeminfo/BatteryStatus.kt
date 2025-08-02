package com.wtc.systeminfo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun BatteryStatusMonitor() {
    val context = LocalContext.current

    var batteryLevel by remember { mutableStateOf("0.0") }
    var chargingStatus by remember { mutableStateOf("未知") }
    var voltage by remember { mutableStateOf("0.0") }
    var current by remember { mutableStateOf("0.0") }
    var power by remember { mutableStateOf("0.0") }

    val intervalOptions = listOf(0.5f, 1.0f, 1.5f)
    var selectedInterval by remember { mutableFloatStateOf(1f) }

    // 1. 实时广播监听充电状态和电量
    DisposableEffect(context) {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryLevel = String.format("%.1f", level * 100f / scale)
                    }

                    val plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    chargingStatus = when (plugged) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
                        else -> "未在充电"
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        onDispose { context.unregisterReceiver(batteryReceiver) }
    }

    val batteryManager = remember {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    // 2. 协程按刷新间隔更新电压、电流、功率
    LaunchedEffect(selectedInterval) {
        while (true) {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1

            if (voltageMv > 0) {
                val voltageV = voltageMv / 1000.0
                voltage = String.format("%.2f", voltageV)

                val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (currentUa != 0) {
                    val currentA = abs(currentUa) / 1_000_000.0
                    current = String.format("%.2f", currentA)
                    power = String.format("%.2f", voltageV * currentA)
                } else {
                    current = "0.0"
                    power = "0.0"
                }
            } else {
                voltage = "0.0"
                current = "0.0"
                power = "0.0"
            }

            delay((selectedInterval * 1000).toLong())
        }
    }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        CustomCard(title = "当前电池容量", description = "$batteryLevel%")
        CustomCard(title = "充电状态", description = chargingStatus)
        CustomCard(title = "电压", description = "$voltage V")
        CustomCard(title = "电流", description = "$current A")
        CustomCard(title = "功率", description = "$power W")

        IntervalSelectorCard(
            currentValue = selectedInterval,
            options = intervalOptions,
            onIntervalChange = { selectedInterval = it }
        )
    }
}
