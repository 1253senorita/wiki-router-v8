package com.teminator.mypadnoteone.indep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.teminator.mypadnoteone.R

class IndepPttActivity : AppCompatActivity() {

    private lateinit var audioEngine: IndepAudioEngine
    private lateinit var streamManager: IndepStreamManager
    private var isTalking = false

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indep_ptt)

        audioEngine = IndepAudioEngine(this)
        streamManager = IndepStreamManager()

        // 진입 시 마이크 권한 체크 및 요청
        checkAudioPermission()

        val targetRoom = intent.getStringExtra("INDEP_TARGET_ROOM") ?: "DEFAULT_ROOM"
        Toast.makeText(this, "[$targetRoom] 독립 PTT 랩실 입장 완료", Toast.LENGTH_SHORT).show()

        setupListeners(targetRoom)
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "마이크 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "마이크 권한이 거부되어 음성 송출을 할 수 없습니다.", Toast.LENGTH_LONG).show()
            }
        }
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
            Toast.makeText(this, "독립 소켓 연결 시도 중...", Toast.LENGTH_SHORT).show()

            streamManager.connect { isSuccess ->
                runOnUiThread {
                    if (isSuccess) {
                        tvStatus.text = "Status: Connected"
                        Toast.makeText(this, "소켓 연결 성공!", Toast.LENGTH_SHORT).show()
                    } else {
                        tvStatus.text = "Status: Connection Failed"
                    }
                }
            }
        }

        // 2. 핑 테스트 버튼
        btnPing.setOnClickListener {
            streamManager.sendPing { latency ->
                runOnUiThread {
                    Toast.makeText(this, "Ping Pong! 응답 속도: ${latency}ms", Toast.LENGTH_SHORT)
                        .show()
                    tvStatus.text = "Status: Ping ${latency}ms"
                }
            }
        }

        // 3. 방 입장 버튼
        btnJoinRoom.setOnClickListener {
            streamManager.joinRoom(targetRoom) { success ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "[$targetRoom] 방 입장 완료", Toast.LENGTH_SHORT).show()
                        tvStatus.text = "Status: In Room [$targetRoom]"
                    }
                }
            }
        }

        // 4. 방 나가기 버튼
        btnLeaveRoom.setOnClickListener {
            streamManager.leaveRoom()
            Toast.makeText(this, "방 퇴장 처리", Toast.LENGTH_SHORT).show()
            tvStatus.text = "Status: Connected (No Room)"
        }

        // 5. PTT 마이크 송출 토글 버튼
        btnPttToggle.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                checkAudioPermission()
                return@setOnClickListener
            }

            isTalking = !isTalking
            if (isTalking) {
                btnPttToggle.text = "🔴 PTT 송출 중지 (마이크 켜짐)"
                Toast.makeText(this, "🎙️ 마이크 녹음 및 음성 스트림 송출 시작", Toast.LENGTH_SHORT).show()

                audioEngine.startRecording { audioData ->
                    if (audioData.isNotEmpty()) {
                        streamManager.sendVoiceData(audioData)
                    }
                }
            } else {
                btnPttToggle.text = "🎙️ PTT 마이크 송출 시작"
                Toast.makeText(this, "⏹️ 마이크 송출 중지", Toast.LENGTH_SHORT).show()
                audioEngine.stopRecording()
            }
        }
    }

    // 💡 onDestroy 함수는 액티비티 클래스 내부, setupListeners 바깥에 올바르게 위치시킵니다.
    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stopRecording()
        streamManager.disconnect()
    }
}