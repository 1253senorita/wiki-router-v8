package com.teminator.mypadnoteone.presentation.phone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        // 앱이 처음 실행될 때 삼성 전화 앱 스타일의 메인 프래그먼트 로드
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.phoneActivityContainer, PhoneMainFragment())
                .commit()
        }
    }
}