package com.wtc.systeminfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader


@Composable
fun SettingsItem(assertManager: AssetManager) {
    val context = LocalContext.current
    val (appName, appVersion) = getAppInfo()

    Column(modifier = Modifier
        .padding(10.dp)) {

        Text(text = "系统信息")
        SystemInfo()
        HardWareInfo(assetManager = assertManager)

        Text(text = "关于软件")
        CustomCard(title = appName, description = "Version: $appVersion")
        CustomCard(title = "Github 仓库", description = "查看软件网址", onClick = {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra("url", "https://www.github.com/wtcpython/System-Info")
            }
            context.startActivity(intent)
        })
    }
}

@Composable
fun getAppInfo(): Pair<String, String> {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val packageName = context.packageName

    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
    val appVersion = packageInfo.versionName

    return Pair(appName, appVersion)
}

@Composable
fun SystemInfo() {
    val context = LocalContext.current

    val version = Build.VERSION.RELEASE

    val process = Runtime.getRuntime().exec(arrayOf("uname", "-r"))
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val kernelVersion = reader.readLine()

    Column {
        CustomCard(title = "Android 版本", description = version)
        CustomCard(title = "Android Kernel", description = kernelVersion, onClick = {
            jumpToAboutPage(context)
        })
    }
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