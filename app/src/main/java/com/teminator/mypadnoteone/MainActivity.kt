package com.teminator.mypadnoteone

import android.content.Intent
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teminator.mypadnoteone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // 1. 웹뷰 기본 설정
        setupWebView()

        // 2. 테스트용 URL 로드 (정상 동작 확인)
        // MainActivity.kt
        // MainActivity.kt (35번째 줄 부근)
        binding.webView.loadUrl("https://penguin-walkie.vercel.app")

        // 3. 새로고침 버튼
        binding.btnReloadWeb.setOnClickListener {
            binding.webView.reload()
            Toast.makeText(this, "웹 화면을 다시 불러옵니다.", Toast.LENGTH_SHORT).show()
        }

        // 4. 로그아웃 버튼
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 5. 최신 안드로이드 뒤로가기 핸들러 설정
        setupOnBackPressed()
    }

    private fun setupWebView() {
        val webView: WebView = binding.webView

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
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