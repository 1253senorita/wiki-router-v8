package com.teminator.mypadnoteone.presentation.aerorouter.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.databinding.ActivityAerorouterEntryBinding
import com.teminator.mypadnoteone.presentation.aerorouter.ui.AeroRouterActivity
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
        // 1. 권한 모드 인증 및 입장 버튼 (get_oi -> 서버가 자동 room.join 처리)
        binding.btnAuthJoin.setOnClickListener {
            val userId = binding.etUserId.text.toString().trim()
            val modeId = binding.etModeOrRoom.text.toString().trim() // 예: DEV_MASTER, GUEST_USER 등
            val userPw = binding.etPassword.text.toString().trim()

            if (userId.isEmpty() || modeId.isEmpty() || userPw.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 서버의 get_oi 이벤트 호출
            socketManager.requestWikiAuth(userId, userPw, modeId) { success, message ->
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        // 인증 및 방 자동 입장 성공 시 -> 무전기 메인 화면(AeroRouterActivity)으로 이동!
                        moveToPttScreen(modeId)
                    }
                }
            }
        }

        // 2. 일반 방 수동 입장 버튼 (join-room 이벤트 직접 호출)
        binding.btnRoomJoin.setOnClickListener {
            val roomId = binding.etModeOrRoom.text.toString().trim()

            if (roomId.isEmpty()) {
                Toast.makeText(this, "입장할 방 번호를 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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