package com.teminator.mypadnoteone.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.teminator.mypadnoteone.presentation.auth.AuthActivity
import com.teminator.mypadnoteone.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // ★ Hilt 의존성 주입 어노테이션
class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val isBypassAuth = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // ★ super.onCreate() 전에 공식 스플래시 화면 설치
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // FirebaseAuth 표준 초기화 방식
        auth = FirebaseAuth.getInstance()

        // 인증 체크 후 바로 화면 이동
        checkAuthAndNavigate()
    }

    private fun checkAuthAndNavigate() {
        val targetClass = if (isBypassAuth || auth.currentUser != null) {
            MainActivity::class.java
        } else {
            AuthActivity::class.java
        }

        val intent = Intent(this, targetClass)
        startActivity(intent)
        finish()
    }
}