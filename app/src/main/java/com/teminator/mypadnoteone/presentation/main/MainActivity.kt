package com.teminator.mypadnoteone.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.teminator.mypadnoteone.presentation.aerorouter.AeroRouterEntryActivity
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

        binding.webView.loadUrl("http://10.0.2.2:8080")
    }



    private fun setupUI() {
        // 1. 무전기 진입 화면(AeroRouterEntryActivity)으로 이동
        binding.btnOpenPtt.setOnClickListener {
            val intent = Intent(this, AeroRouterEntryActivity::class.java)
            startActivity(intent)
        }

        // 2. 웹 새로고침
        binding.btnReloadWeb.setOnClickListener {
            binding.webView.reload()
        }

        // 3. 로그아웃 (스탤라 인증 화면으로 이동)
        binding.btnLogout.setOnClickListener {
            viewModel.signOut()
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
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}