package com.teminator.mypadnoteone.presentation.aerorouter.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.databinding.ActivityAerorouterEntryBinding
import com.teminator.mypadnoteone.presentation.aerorouter.socket.AeroSocketManager

class AeroRouterEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAerorouterEntryBinding
    private lateinit var socketManager: AeroSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAerorouterEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        socketManager = AeroSocketManager()

        // 서버 소켓 연결 수행
        socketManager.connect(
            onConnected = {
                runOnUiThread {
                    Toast.makeText(this, "서버 연결 성공!", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { err ->
                runOnUiThread {
                    Toast.makeText(this, "서버 연결 실패: $err", Toast.LENGTH_SHORT).show()
                }
            }
        )

        setupListeners()
    }

    private fun setupListeners() {
        // 1. 권한 모드 인증 및 입장 버튼 (잠금 해제: 항상 성공(true) 처리로 바로 통과)
        binding.btnAuthJoin.setOnClickListener {
            val modeId = binding.etModeOrRoom.text.toString().trim().ifEmpty { "DEV_MASTER" }

            Toast.makeText(this, "권한 모드 잠금 해제됨 (무조건 입장)", Toast.LENGTH_SHORT).show()

            // 서버 인증을 거치지 않고 곧바로 PTT 화면으로 이동
            moveToPttScreen(modeId)
        }

        // 2. 일반 방 수동 입장 버튼 (join-room 이벤트 직접 호출)
        binding.btnRoomJoin.setOnClickListener {
            val roomId = binding.etModeOrRoom.text.toString().trim().ifEmpty { "DEFAULT_ROOM" }

            // 서버의 join-room 이벤트 호출
            socketManager.joinRoom(roomId)
            Toast.makeText(this, "[$roomId] 방으로 입장 시도 중...", Toast.LENGTH_SHORT).show()

            // 무전기 화면으로 이동
            moveToPttScreen(roomId)
        }
    }

    private fun moveToPttScreen(roomOrMode: String) {
        val intent = Intent(this, AeroRouterActivity::class.java).apply {
            putExtra("ROOM_ID", roomOrMode)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }
}