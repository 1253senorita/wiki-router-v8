package com.teminator.mypadnoteone.indep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
    private val logBuffer = StringBuilder()
    private lateinit var tvIndepLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indep_ptt)

        audioEngine = IndepAudioEngine(this)
        streamManager = IndepStreamManager()

        tvIndepLog = findViewById(R.id.tvIndepLog)
        checkAudioPermission()

        val targetRoom = intent.getStringExtra("INDEP_TARGET_ROOM") ?: "DEFAULT_ROOM"
        appendLog("[$targetRoom] 위키-라우터 PTT 랩실 입장 완료")

        setupListeners(targetRoom)

        // 텍스트 메시지 수신 리스너
        streamManager.onMessageReceived { senderId, message ->
            appendLog("상대방($senderId): $message")
        }

        // 상대방 오디오 스트림 수신 리스너
        streamManager.onAudioReceived { audioBytes ->
            if (audioBytes.isNotEmpty()) {
                appendLog("🔊 상대방 음성 데이터 수신됨 (${audioBytes.size} bytes)")
                // TODO: 추후 AudioTrack을 이용한 스피커 재생 로직 연결 지점
            }
        }
    }

    private fun appendLog(message: String) {
        runOnUiThread {
            logBuffer.append("$message\n")
            tvIndepLog.text = logBuffer.toString()
        }
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

    private fun setupListeners(targetRoom: String) {
        val tvStatus = findViewById<TextView>(R.id.tvIndepStatus)
        val btnConnect = findViewById<Button>(R.id.btnIndepConnect)
        val btnPing = findViewById<Button>(R.id.btnIndepPing)
        val btnJoinRoom = findViewById<Button>(R.id.btnIndepJoinRoom)
        val btnLeaveRoom = findViewById<Button>(R.id.btnIndepLeaveRoom)
        val btnPttToggle = findViewById<Button>(R.id.btnIndepPttToggle)

        val etMessage = findViewById<EditText>(R.id.etIndepMessage)
        val btnSendChat = findViewById<Button>(R.id.btnIndepSendChat)

        btnConnect.setOnClickListener {
            tvStatus.text = "Status: Connecting..."
            appendLog("위키-라우터 소켓 연결 시도 중...")

            streamManager.connect { isSuccess ->
                runOnUiThread {
                    if (isSuccess) {
                        tvStatus.text = "Status: Connected"
                        appendLog("소켓 연결 성공!")
                    } else {
                        tvStatus.text = "Status: Connection Failed"
                        appendLog("소켓 연결 실패")
                    }
                }
            }
        }

        btnPing.setOnClickListener {
            streamManager.sendPing { latency ->
                runOnUiThread {
                    appendLog("Ping Pong! 응답 속도: ${latency}ms")
                    tvStatus.text = "Status: Ping ${latency}ms"
                }
            }
        }

        btnJoinRoom.setOnClickListener {
            streamManager.joinRoom(targetRoom) { success ->
                runOnUiThread {
                    if (success) {
                        appendLog("[$targetRoom] 방 입장 완료")
                        tvStatus.text = "Status: In Room [$targetRoom]"
                    }
                }
            }
        }

        btnLeaveRoom.setOnClickListener {
            streamManager.leaveRoom()
            appendLog("방 퇴장 처리 완료")
            tvStatus.text = "Status: Connected (No Room)"
        }

        btnSendChat.setOnClickListener {
            val msgText = etMessage.text.toString().trim()
            if (msgText.isNotEmpty()) {
                streamManager.sendChatMessage(msgText, "OppaUser") { success ->
                    runOnUiThread {
                        if (success) {
                            appendLog("나: $msgText")
                            etMessage.setText("")
                        } else {
                            appendLog("메시지 전송 실패")
                        }
                    }
                }
            } else {
                Toast.makeText(this, "메시지를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        btnPttToggle.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                appendLog("마이크 권한이 필요합니다.")
                checkAudioPermission()
                return@setOnClickListener
            }

            isTalking = !isTalking
            if (isTalking) {
                btnPttToggle.text = "🔴 PTT 송출 중지 (마이크 켜짐)"
                appendLog("🎙️ 마이크 녹음 및 음성 스트림 송출 시작")

                audioEngine.startRecording { audioData ->
                    if (audioData.isNotEmpty()) {
                        streamManager.sendVoiceData(audioData)
                    }
                }
            } else {
                btnPttToggle.text = "🎙️ PTT 마이크 송출 시작"
                appendLog("⏹️ 마이크 송출 중지")
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