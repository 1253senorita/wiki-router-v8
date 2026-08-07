package com.teminator.mypadnoteone.presentation.wiki.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.databinding.ActivityWikiBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WikiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWikiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWikiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWikiWebView()
        setupWikiOnBackPressed()

        // 메인 웹뷰 서비스 주소(10.0.2.2:8080) 또는 위키 전용 주소 로드
        binding.webViewWiki.loadUrl("https://10.0.2.2:8080")
    }

    private fun setupWikiWebView() {
        binding.webViewWiki.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: android.webkit.SslErrorHandler?,
                error: android.net.http.SslError?
            ) {
                handler?.proceed() // 사설 인증서 허용
            }
        }

        binding.webViewWiki.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                runOnUiThread { request?.grant(request.resources) }
            }
        }

        // 다운로드 리스너 추가 (필요시 조건문 수정 가능)
        binding.webViewWiki.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            if (url.endsWith(".zip") || url.contains("download")) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "다운로드 링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "지원하지 않는 다운로드 형식입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        val webSettings: WebSettings = binding.webViewWiki.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        @Suppress("DEPRECATION")
        webSettings.databaseEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.mediaPlaybackRequiresUserGesture = false
    }

    private fun setupWikiOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webViewWiki.canGoBack()) {
                    binding.webViewWiki.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}