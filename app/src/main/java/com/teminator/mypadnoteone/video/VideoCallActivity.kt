package com.teminator.mypadnoteone.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 임시로 기본 빈 레이아웃을 쓰거나 뷰를 생성할 수 있습니다.
        // 추후 영상 통화 레이아웃(activity_video_call.xml 등)을 만들어 연결하시면 됩니다.
        setContentView(R.layout.activity_main) // 에러 방지용 임시 레이아웃 (필요시 전용 xml 생성 후 교체)
    }
}