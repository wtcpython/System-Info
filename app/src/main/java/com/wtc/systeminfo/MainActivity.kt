package com.wtc.systeminfo

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StatFs
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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
                    MainContentWithDialog()
                }
            }
        }
    }
}

@Composable
fun MainContentWithDialog()
{
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 延迟显示对话框
        delay(500)
        showDialog = true
    }

    MainContent()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ){
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth() // 占据整个屏幕宽度
                            .height(500.dp), // 设定高度，可以根据需要调整
                        factory = { context ->
                            VideoView(context).apply {
                                setVideoURI("https://fastcdn.mihoyo.com/content-v2/hkrpg/101956/8f827d2d9b7340c9b8277176dd0dd83d_6578312574948238203.mp4".toUri())
                                setOnCompletionListener {
                                    start() // 视频播放完成后重新播放
                                }
                                start()
                            }
                        }
                    )
                    TextButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        onClick = { showDialog = false }) {
                        Text("关闭")
                    }
                }
            },
            confirmButton = {}
        )
    }
}


@Composable
fun MainContent() {
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
                2 -> SettingsItem()
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