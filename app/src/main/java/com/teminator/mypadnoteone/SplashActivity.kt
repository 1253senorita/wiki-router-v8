package com.teminator.mypadnoteone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    // ★ 개발 우회 스위치: true로 두면 로그인 화면을 패스하고 바로 MainActivity로 직행합니다!
    private val isBypassAuth = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1.5초(1500ms) 동안 스플래시 화면을 보여준 뒤 분기 처리
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, 1500)
    }

    private fun checkAuthAndNavigate() {
        if (isBypassAuth) {
            // 메인 화면으로 이동
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 인증/로그인 화면으로 이동
            startActivity(Intent(this, AuthActivity::class.java))
        }
        // 스플래시 화면 종료 (뒤로가기 스택에서 제거)
        finish()
    }
}