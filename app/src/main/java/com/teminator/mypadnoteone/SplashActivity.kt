package com.teminator.mypadnoteone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teminator.mypadnoteone.databinding.ActivitySplashBinding



class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    // ★ 개발 중에는 true로 세팅하면 파이어베이스 체크 패스하고 직행 가능!
    private val isBypassAuth = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, 1500)
    }

    private fun checkAuthAndNavigate() {
        val intent = if (isBypassAuth || auth.currentUser != null) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AuthActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}