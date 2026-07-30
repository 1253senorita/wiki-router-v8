package com.teminator.mypadnoteone

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.teminator.mypadnoteone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 최신 Firebase Auth 초기화 방식
        auth = Firebase.auth

        // 1. 마이크 OS 런타임 권한 확인 및 요청
        checkAudioPermission()

        // 2. 웹뷰 기본 설정
        setupWebView()

        // 3. 테스트용 URL 로드
        binding.webView.loadUrl("http://10.0.2.2:8080")

        // 4. 새로고침 버튼
        binding.btnReloadWeb.setOnClickListener {
            binding.webView.reload()
            Toast.makeText(this, "웹 화면을 다시 불러옵니다.", Toast.LENGTH_SHORT).show()
        }

        // 5. 로그아웃 버튼 (안전한 Intent 생성 방식 적용)
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

            // 방법 A: Intent 생성 시 명시적 ComponentName 지정
            val intent = Intent().setClassName(this, "com.teminator.mypadnoteone.AuthActivity")

            // 또는 방법 B: KClass 확장 구문 활용
            // val intent = Intent(this, AuthActivity::class.javaObjectType)

            startActivity(intent)
            finish()
        }

        // 6. 최신 안드로이드 뒤로가기 핸들러 설정
        setupOnBackPressed()
    }

    // OS 레벨 마이크 권한 요청
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
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "마이크 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
                binding.webView.reload() // 권한 승인 후 웹뷰 새로고침
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
                // 웹뷰 내부에서 마이크/카메라 등의 권한을 요청할 때 자동으로 승인
                runOnUiThread {
                    request?.grant(request.resources)
                }
            }
        }

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true

        // HTTP/HTTPS 혼합 콘텐츠 및 미디어 자동 재생 허용
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