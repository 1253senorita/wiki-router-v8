package com.teminator.mypadnoteone.presentation.aerorouter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.teminator.mypadnoteone.databinding.ActivityAerorouterBinding

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
        // 오디오 엔진 초기화: 마이크 데이터가 잡히면 소켓이나 서버로 전송할 위치
        audioEngine = AeroAudioEngine { buffer, length ->
            // TODO: 송신 상태일 때 socketManager를 통해 실시간 바이너리/데이터 스트리밍 전송
        }

        socketManager = AeroSocketManager()

        // 서버 연결 시도
        socketManager.connect(
            onConnected = {
                runOnUiThread {
                    isConnected = true
                    binding.tvStatus.text = "CONNECTED (STANDBY)"
                    Toast.makeText(this, "AeroRouter 서버 연결 성공!", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                runOnUiThread {
                    isConnected = false
                    binding.tvStatus.text = "DISCONNECTED"
                }
            }
        )
    }

    private fun setupListeners() {
        // PTT 버튼 터치 이벤트 (누를 때 송신, 뗄 때 정지)
        binding.btnPtt.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    if (!isConnected) {
                        Toast.makeText(this, "서버가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        return@setOnTouchListener true
                    }
                    // 눌렸을 때 디자인 변경 및 녹음 시작
                    binding.btnPtt.setBackgroundResource(com.teminator.mypadnoteone.R.drawable.neumorphic_button_pressed)
                    binding.tvStatus.text = "TALKING (송신 중...)"
                    audioEngine.startRecording()
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    // 뗄 때 원상복구 및 녹음 정지
                    binding.btnPtt.setBackgroundResource(com.teminator.mypadnoteone.R.drawable.neumorphic_button_normal)
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