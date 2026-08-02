package com.teminator.mypadnoteone.presentation.ptt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.teminator.mypadnoteone.databinding.ActivityPttSectionBinding
import com.teminator.mypadnoteone.presentation.main.PttJavascriptInterface
import com.teminator.mypadnoteone.service.PttService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PttSectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPttSectionBinding

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 200
        private const val PTT_ENGINE_URL = "http://10.0.2.2:8080"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPttSectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkAndRequestPermissions()
        setupWebView()
        setupListeners()
        setupOnBackPressed()

        binding.webView.loadUrl(PTT_ENGINE_URL)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            stopPttForegroundService()
            finish()
        }

        binding.btnReloadWeb.setOnClickListener {
            binding.webView.reload()
            Toast.makeText(this, "PTT 엔진을 다시 불러옵니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 마이크 및 Android 13+ 알림 권한 다중 체크
    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                REQUEST_PERMISSIONS_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "필수 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
                binding.webView.reload()
            } else {
                Toast.makeText(this, "PTT 음성 통신 및 알림 권한 허용이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupWebView() {
        val webView: WebView = binding.webView

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                runOnUiThread {
                    request?.grant(request.resources)
                }
            }
        }

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        @Suppress("DEPRECATION")
        webSettings.databaseEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.mediaPlaybackRequiresUserGesture = false

        // JS Bridge 연결: PTT 송수신 이벤트 발생 시 포그라운드 서비스 시작/종료 처리
        webView.addJavascriptInterface(
            PttJavascriptInterface(this) { isActive ->
                if (isActive) {
                    startPttForegroundService()
                } else {
                    stopPttForegroundService()
                }
            },
            "AndroidPTT"
        )
    }

    private fun startPttForegroundService() {
        val serviceIntent = Intent(this, PttService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopPttForegroundService() {
        val serviceIntent = Intent(this, PttService::class.java)
        stopService(serviceIntent)
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    stopPttForegroundService()
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}