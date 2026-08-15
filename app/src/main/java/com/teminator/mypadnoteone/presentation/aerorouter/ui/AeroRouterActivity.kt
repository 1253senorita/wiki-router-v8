package com.teminator.mypadnoteone.presentation.aerorouter.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.teminator.mypadnoteone.R
import com.teminator.mypadnoteone.databinding.ActivityAerorouterBinding
import com.teminator.mypadnoteone.presentation.aerorouter.audio.AeroAudioEngine
import com.teminator.mypadnoteone.presentation.aerorouter.socket.AeroSocketManager

class AeroRouterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAerorouterBinding
    private lateinit var socketManager: AeroSocketManager
    private lateinit var audioEngine: AeroAudioEngine

    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAerorouterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermission()
        initEngineAndSocket()
        setupListeners()
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }

    private fun initEngineAndSocket() {
        // 오디오 엔진 초기화: 마이크 버퍼 데이터를 실시간으로 socketManager의 sendAudioData와 연동
        audioEngine = AeroAudioEngine { buffer, length ->
            if (isConnected) {
                // 정확한 크기만큼의 바이트 배열로 잘라서 소켓 전송 함수 호출
                val actualData =
                    if (length == buffer.size) buffer else buffer.copyOfRange(0, length)
                socketManager.sendAudioData(actualData)
            }
        }

        socketManager = AeroSocketManager()

        // 서버 연결 시도
        socketManager.connect(
            onConnected = {
                runOnUiThread {
                    isConnected = true
                    binding.tvStatus.text = "CONNECTED (STANDBY)"
                    Toast.makeText(this@AeroRouterActivity, "AeroRouter 서버 연결 성공!", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                runOnUiThread {
                    isConnected = false
                    binding.tvStatus.text = "DISCONNECTED"
                    Toast.makeText(this@AeroRouterActivity, "서버 연결 실패: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupListeners() {
        // PTT 버튼 터치 이벤트 (누를 때 송신, 뗄 때 정지)
        binding.btnPtt.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isConnected) {
                        Toast.makeText(this, "서버가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        return@setOnTouchListener true
                    }
                    // 눌렸을 때 디자인 변경 및 녹음 시작
                    binding.btnPtt.setBackgroundResource(R.drawable.neumorphic_button_pressed)
                    binding.tvStatus.text = "TALKING (송신 중...)"
                    audioEngine.startRecording()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 뗄 때 원상복구 및 녹음 정지
                    binding.btnPtt.setBackgroundResource(R.drawable.neumorphic_button_normal)
                    binding.tvStatus.text = "CONNECTED (STANDBY)"
                    audioEngine.stopRecording()
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
        socketManager.disconnect()
    }
}