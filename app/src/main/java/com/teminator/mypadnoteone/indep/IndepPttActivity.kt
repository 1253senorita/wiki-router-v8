package com.teminator.mypadnoteone.indep

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.R

class IndepPttActivity : AppCompatActivity() {

    private lateinit var audioEngine: IndepAudioEngine
    private lateinit var streamManager: IndepStreamManager
    private var isTalking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 방금 만든 뉴모피즘 통신 테스트 레이아웃 연결
        setContentView(R.layout.activity_indep_ptt)

        audioEngine = IndepAudioEngine(this)
        streamManager = IndepStreamManager()

        val targetRoom = intent.getStringExtra("INDEP_TARGET_ROOM") ?: "DEFAULT_ROOM"
        Toast.makeText(this, "[$targetRoom] 독립 PTT 랩실 입장 완료", Toast.LENGTH_SHORT).show()

        setupListeners(targetRoom)
    }

    private fun setupListeners(targetRoom: String) {
        val tvStatus = findViewById<TextView>(R.id.tvIndepStatus)
        val btnConnect = findViewById<Button>(R.id.btnIndepConnect)
        val btnPing = findViewById<Button>(R.id.btnIndepPing)
        val btnJoinRoom = findViewById<Button>(R.id.btnIndepJoinRoom)
        val btnLeaveRoom = findViewById<Button>(R.id.btnIndepLeaveRoom)
        val btnPttToggle = findViewById<Button>(R.id.btnIndepPttToggle)

        // 1. 소켓 연결 버튼
        btnConnect.setOnClickListener {
            tvStatus.text = "Status: Connecting..."
            streamManager.connect()
            Toast.makeText(this, "독립 소켓 연결 시도 중...", Toast.LENGTH_SHORT).show()
            tvStatus.text = "Status: Connected"
        }

        // 2. 핑 테스트 버튼
        btnPing.setOnClickListener {
            Toast.makeText(this, "Ping 전송 완료 (Pong 수신 대기)", Toast.LENGTH_SHORT).show()
        }

        // 3. 방 입장 버튼
        btnJoinRoom.setOnClickListener {
            Toast.makeText(this, "[$targetRoom] 방 입장 요청 전송", Toast.LENGTH_SHORT).show()
            tvStatus.text = "Status: Joined Room [$targetRoom]"
        }

        // 4. 방 나가기 버튼
        btnLeaveRoom.setOnClickListener {
            Toast.makeText(this, "방 퇴장 처리", Toast.LENGTH_SHORT).show()
            tvStatus.text = "Status: Connected (No Room)"
        }

        // 5. PTT 마이크 송출 토글 버튼
        btnPttToggle.setOnClickListener {
            isTalking = !isTalking
            if (isTalking) {
                btnPttToggle.text = "🔴 PTT 송출 중지 (마이크 켜짐)"
                Toast.makeText(this, "🎙️ 마이크 녹음 및 음성 스트림 송출 시작", Toast.LENGTH_SHORT).show()

                audioEngine.startRecording { audioData ->
                    streamManager.sendVoiceData(audioData)
                }
            } else {
                btnPttToggle.text = "🎙️ PTT 마이크 송출 시작"
                Toast.makeText(this, "⏹️ 마이크 송출 중지", Toast.LENGTH_SHORT).show()
                audioEngine.stopRecording()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stopRecording()
        streamManager.disconnect()
    }
}