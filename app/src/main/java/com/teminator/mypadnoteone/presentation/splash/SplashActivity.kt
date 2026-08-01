package com.teminator.mypadnoteone.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.teminator.mypadnoteone.presentation.auth.AuthActivity
import com.teminator.mypadnoteone.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    // ★ Hilt를 통해 SplashViewModel 주입
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // super.onCreate() 전에 공식 스플래시 화면 설치
        installSplashScreen()

        super.onCreate(savedInstanceState)

        checkAuthAndNavigate()
    }

    private fun checkAuthAndNavigate() {
        val targetClass = if (viewModel.shouldNavigateToMain()) {
            MainActivity::class.java
        } else {
            AuthActivity::class.java
        }

        val intent = Intent(this, targetClass)
        startActivity(intent)
        finish()
    }
}