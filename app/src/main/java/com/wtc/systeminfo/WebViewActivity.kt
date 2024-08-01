package com.wtc.systeminfo

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)

        val webView: WebView = findViewById(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            javaScriptCanOpenWindowsAutomatically = true
        }

        webView.webViewClient = WebViewClient()

        val url = intent.getStringExtra("url")

        url?.let {
            webView.loadUrl(it)
        }

        val exitButton: Button = findViewById(R.id.exit_button)
        exitButton.setOnClickListener {
            val intent = Intent(this@WebViewActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // 结束 WebViewActivity
        }
    }
}
