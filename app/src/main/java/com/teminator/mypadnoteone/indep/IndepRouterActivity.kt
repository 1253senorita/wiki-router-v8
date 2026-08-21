package com.teminator.mypadnoteone.indep

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.R
import com.teminator.mypadnoteone.databinding.ActivityAerorouterEntryBinding // 기존 레이아웃 재사용

class IndepRouterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAerorouterEntryBinding
    private lateinit var streamManager: IndepStreamManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 기존 엔트리 레이아웃을 그대로 사용하여 화면 구성
        binding = ActivityAerorouterEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        streamManager = IndepStreamManager()

        // 독립 소켓 연결 시도 (임시)
        streamManager.connect()

        setupListeners()
    }

    private fun setupListeners() {
        // 1. 권한 모드 접속 버튼 (임시 우회: 조건 없이 무조건 통과)
        binding.btnAuthJoin.setOnClickListener {
            val modeId = binding.etModeOrRoom.text.toString().trim().ifEmpty { "INDEP_MASTER" }

            Toast.makeText(this, "[$modeId] 독립 권한 모드 임시 허용 통과!", Toast.LENGTH_SHORT).show()

            // 🌟 독립 PTT 화면으로 이동
            moveToIndepPtt(modeId)
        }

        // 2. 일반 방 수동 입장 버튼 (임시 우회)
        binding.btnRoomJoin.setOnClickListener {
            val roomId = binding.etModeOrRoom.text.toString().trim().ifEmpty { "INDEP_ROOM" }

            Toast.makeText(this, "[$roomId] 독립 방으로 임시 입장합니다.", Toast.LENGTH_SHORT).show()

            // 🌟 독립 PTT 화면으로 이동
            moveToIndepPtt(roomId)
        }
    }

    /**
     * 독립 PTT 통신 화면으로 이동하는 메서드
     */
    private fun moveToIndepPtt(target: String) {
        val intent = Intent(this, IndepPttActivity::class.java).apply {
            putExtra("INDEP_TARGET_ROOM", target)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        streamManager.disconnect()
    }
}