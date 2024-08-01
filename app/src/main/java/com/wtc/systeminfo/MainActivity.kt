package com.wtc.systeminfo

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.wtc.systeminfo.ui.theme.SystemInfoTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        createNotificationChannel(this)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        setContent {
            SystemInfoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                     ScaffoldWithBottomNavigation(assets)
                }
            }
        }
    }
}


@Composable
fun ScaffoldWithBottomNavigation(assertManager: AssetManager) {
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Build, contentDescription = "Build") },
                    label = { Text("Build") },
                    selected = selectedItem == 1,
                    onClick = { selectedItem = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedItem == 2,
                    onClick = { selectedItem = 2 }
                )
            }
        }
    ) { innerPadding ->
        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (selectedItem) {
                0 -> MemoryStorageInfo()
                1 -> BatteryStatusMonitor()
                2 -> SettingsItem(assertManager)
            }
        }
    }
}

@Composable
fun MemoryStorageInfo() {
    val context = LocalContext.current
    var totalMemory by remember { mutableDoubleStateOf(0.0) }
    var availableMemory by remember { mutableDoubleStateOf(0.0) }
    var totalStorage by remember { mutableDoubleStateOf(0.0) }
    var availableStorage by remember { mutableDoubleStateOf(0.0) }

    val num = 1024.0

    LaunchedEffect(Unit) {
        val activityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        // Set up periodic updates
        while (true) {
            // Update memory info
            activityManager.getMemoryInfo(memoryInfo)
            totalMemory = memoryInfo.totalMem / num / num / num
            availableMemory = memoryInfo.availMem / num / num / num

            // Update storage info
            val storageStatFs = StatFs(context.filesDir.absolutePath)
            totalStorage = storageStatFs.totalBytes / num / num / num
            availableStorage = storageStatFs.availableBytes / num / num / num
            delay(1000) // Update every 1 second
        }
    }

    Column {
        CustomCard(title = "总内存", description = "%.2f GB".format(totalMemory))
        CustomCard(title = "可用内存", description = "%.2f GB".format(availableMemory))
        CustomCard(title = "总存储", description = "%.2f GB".format(totalStorage))
        CustomCard(title = "可用存储", description = "%.2f GB".format(availableStorage))
        NotificationButton()
    }
}

@Composable
fun BatteryStatusMonitor() {
    val context = LocalContext.current
    var batteryLevel by remember { mutableStateOf("0.0") }
    var chargingStatus by remember { mutableStateOf("未知") }

    // Create a BroadcastReceiver to listen for battery changes
    val batteryReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    // Update battery level
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPct = if (level != -1 && scale != -1) {
                        (level.toFloat() / scale) * 100.0f
                    } else {
                        0.0f
                    }
                    batteryLevel = String.format("%.1f", batteryPct)

                    // Update charging status
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    chargingStatus = when (plugged) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
                        else -> "未在充电"
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, intentFilter)
    }

    DisposableEffect(Unit) {
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }

    Column {
        CustomCard(title = "当前电池容量", description = "$batteryLevel%")
        CustomCard(title = "充电状态", description = chargingStatus)
    }
}

@Composable
fun NotificationButton() {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            sendNotification(context)
        }) {
            Text("发送通知")
        }
    }
}