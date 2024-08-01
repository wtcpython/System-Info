package com.wtc.systeminfo

import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.wtc.systeminfo.ui.theme.SystemInfoTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var lastUpdate: Long = 0
    private var lastX: Float = 0.toFloat()
    private var lastY: Float = 0.toFloat()
    private var lastZ: Float = 0.toFloat()

    private val shakeThreshold = 200f // 震动阈值

    private val mainScope = MainScope()
    private var skipped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SystemInfoTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.splash_background),
                        contentDescription = "Splash Window",
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable {
                                openWebPage("https://zzz.mihoyo.com/main")
                            },
                        contentScale = ContentScale.Crop
                    )

                    Button(
                        onClick = {
                            openWebPage("https://ys.mihoyo.com")
                        },
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("×", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }


                    Button(
                        onClick = {
                            skipped = true
                            goToMainActivity()
                        },
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Exit", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 初始化传感器管理器
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // 使用 Coroutine 来延迟启动主活动
        mainScope.launch {
            delay(5000)
            if (!skipped) {
                // 启动主活动
                goToMainActivity()
            }
        }
    }

    // 摇晃事件处理函数
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val curTime = System.currentTimeMillis()

            if ((curTime - lastUpdate) > 100) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val diffTime = curTime - lastUpdate
                lastUpdate = curTime

                val speed = abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000.0

                if (speed > shakeThreshold) {
                    // 如果摇晃速度超过阈值，则打开网页
                    if (!skipped) {
                        openWebPage("https://sr.mihoyo.com")
                        skipped = true
                    }
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun openWebPage(url: String) {
        skipped = true
        startActivity(Intent(this, WebViewActivity::class.java).apply {
            putExtra("url", url)
        })
        finish()
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}