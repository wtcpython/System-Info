package com.wtc.systeminfo

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wtc.systeminfo.ui.theme.SystemInfoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

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
                    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 30.dp)) {
                        item {
                            BatteryStatusMonitor()
                            MemoryStorageInfo()
                            SystemInfo()
                            HardWareInfo(assets)
                            NotificationButton()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomCard(title: String, description: String, onClick: (() -> Unit) = {  }) {
    Card(modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(),
        onClick = onClick)
    {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f))
            Text(description, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
        }
    }
}

@SuppressLint("DefaultLocale")
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

    CustomCard(title = "当前电池容量", description = "$batteryLevel%")
    CustomCard(title = "充电状态", description = chargingStatus)
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
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
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
            delay(1000) // Update every 5 seconds
        }
    }

    CustomCard(title = "总内存", description = "%.2f GB".format(totalMemory))
    CustomCard(title = "可用内存", description = "%.2f GB".format(availableMemory))
    CustomCard(title = "总存储", description = "%.2f GB".format(totalStorage))
    CustomCard(title = "可用存储", description = "%.2f GB".format(availableStorage))
}

@Composable
fun SystemInfo() {
    val context = LocalContext.current

    val version = Build.VERSION.RELEASE

    val process = Runtime.getRuntime().exec(arrayOf("uname", "-r"))
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val kernelVersion = reader.readLine()

    CustomCard(title = "Android 版本", description = version)
    CustomCard(title = "Android Kernel", description = kernelVersion, onClick = {
        jumpToAboutPage(context)
    })
}

@SuppressLint("QueryPermissionsNeeded")
fun jumpToAboutPage(context: Context) {
    val aboutIntent = Intent()
    aboutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    aboutIntent.action = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
    if (aboutIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(aboutIntent)
    }
}

@Composable
fun HardWareInfo(assetManager: AssetManager) {
    val scope = rememberCoroutineScope()

    var chipsetName by remember { mutableStateOf("Unknown") }

    LaunchedEffect(key1 = assetManager, block = {
        scope.launch(Dispatchers.IO) {
            val json = withContext(Dispatchers.IO) {
                readJsonFile(assetManager, "soc.json")
            }
            val typeToken = object : TypeToken<Map<String, String>>() {}.type
            val processorsMap = Gson().fromJson(json, typeToken) as Map<String, String>

            val socModel = Build.SOC_MODEL
            val processorName = processorsMap[socModel] ?: processorsMap["$socModel-AB"] ?: socModel
            withContext(Dispatchers.Main) {
                chipsetName = processorName
            }
        }
    })

    CustomCard(title = "处理器型号", description = chipsetName)
}

private fun readJsonFile(assetManager: AssetManager, fileName: String): String {
    return assetManager.open(fileName).use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.readText()
        }
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