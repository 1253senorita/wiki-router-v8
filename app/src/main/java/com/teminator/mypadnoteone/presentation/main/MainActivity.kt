package com.teminator.mypadnoteone.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.teminator.mypadnoteone.databinding.ActivityMainBinding
import com.teminator.mypadnoteone.presentation.auth.AuthActivity
import com.teminator.mypadnoteone.presentation.aerorouter.ui.AeroRouterEntryActivity
import com.teminator.mypadnoteone.presentation.wiki.ui.WikiActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Hilt를 통한 ViewModel 주입
    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermission()
        setupWebView()
        setupUI()
        setupObserve()

        // 1. 기존 메인 웹뷰 로드 (10.0.2.2:8080)
        binding.webView.loadUrl("https://10.0.2.2:8080")

        // 2. 추가된 세컨드 웹뷰에 파이어베이스 호스팅 주소 로드
        binding.webViewSecondary.loadUrl("https://mypadnoteone-a7ca4.web.app")
    }

    private fun setupUI() {
        // 1. 무전기 진입 화면(AeroRouterEntryActivity)으로 이동
        binding.btnOpenPtt.setOnClickListener {
            val intent = Intent(this, AeroRouterEntryActivity::class.java)
            startActivity(intent)
        }

        // 2. 웹 새로고침 (두 웹뷰 모두 새로고침)
        binding.btnReloadWeb.setOnClickListener {
            binding.webView.reload()
            binding.webViewSecondary.reload()
            Toast.makeText(this, "웹페이지를 새로고침했습니다.", Toast.LENGTH_SHORT).show()
        }

        // 3. 로그아웃 (스탤라 인증 화면으로 이동)
        binding.btnLogout.setOnClickListener {
            viewModel.signOut()
        }

        // Wiki 라우터 화면으로 이동
        binding.btnWIKIPtt.setOnClickListener {
            val intent = Intent(this, WikiActivity::class.java)
            startActivity(intent)
        }


        setupOnBackPressed()
    }

    private fun setupObserve() {
        lifecycleScope.launch {
            viewModel.uiEvent
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        is MainUiEvent.NavigateToAuth -> {
                            Toast.makeText(this@MainActivity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivity, AuthActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            // 기타 이벤트 처리
                        }
                    }
                }
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "마이크 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
                binding.webView.reload()
            } else {
                Toast.makeText(this, "무전기 기능을 사용하려면 마이크 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupWebView() {
        // 공통 웹뷰 세팅 함수 설정 (메인 및 세컨드 웹뷰 모두 적용)
        val configureWebView = { webView: WebView ->
            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(
                    view: WebView?,
                    handler: android.webkit.SslErrorHandler?,
                    error: android.net.http.SslError?
                ) {
                    // 개발 환경에서 mkcert 사설 인증서 허용 처리
                    handler?.proceed()
                }
            }

            webView.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    runOnUiThread {
                        request?.grant(request.resources)
                    }
                }
            }

            // 웹뷰 다운로드 리스너 설정 (조건부 링크 처리)
            webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                // 원하는 다운로드 조건 (.zip 파일이거나 download 키워드가 포함된 경우)
                if (url.endsWith(".zip") || url.contains("download")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "다운로드 링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "지원하지 않는 다운로드 형식입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            @Suppress("DEPRECATION")
            webSettings.databaseEnabled = true
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webSettings.mediaPlaybackRequiresUserGesture = false
        }

        // 두 웹뷰에 모두 적용
        configureWebView(binding.webView)
        configureWebView(binding.webViewSecondary)
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 시 두 웹뷰 중 먼저 히스토리가 있는 쪽을 뒤로가기 수행
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else if (binding.webViewSecondary.canGoBack()) {
                    binding.webViewSecondary.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}