package com.teminator.mypadnoteone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.teminator.mypadnoteone.databinding.ActivityMainBinding // ★ ViewBinding 클래스 import

class MainActivity : AppCompatActivity() {

    // 1. 바인딩 객체 변수 선언
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. XML을 인플레이트해서 바인딩 객체 생성
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 3. R.layout... 대신 binding.root를 화면에 설정
        setContentView(binding.root)

        // 4. findViewById(R.id.main) 대신 binding.main으로 깔끔하게 접근!
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}