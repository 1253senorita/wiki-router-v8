package com.teminator.mypadnoteone.indep

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.databinding.ActivityAerorouterEntryBinding

class IndepRouterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAerorouterEntryBinding
    private lateinit var streamManager: IndepStreamManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAerorouterEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        streamManager = IndepStreamManager()

        streamManager.connect { isSuccess ->
            runOnUiThread {
                if (isSuccess) {
                    Toast.makeText(this, "서버 소켓 연결 성공!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "서버 소켓 연결 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnAuthJoin.setOnClickListener {
            val modeId = binding.etModeOrRoom.text.toString().trim().ifEmpty { "INDEP_MASTER" }
            Toast.makeText(this, "[$modeId] 독립 권한 모드 임시 허용 통과!", Toast.LENGTH_SHORT).show()
            moveToIndepPtt(modeId)
        }

        binding.btnRoomJoin.setOnClickListener {
            val roomId = binding.etModeOrRoom.text.toString().trim().ifEmpty { "INDEP_ROOM" }
            Toast.makeText(this, "[$roomId] 독립 방으로 임시 입장합니다.", Toast.LENGTH_SHORT).show()
            moveToIndepPtt(roomId)
        }
    }

    private fun moveToIndepPtt(target: String) {
        val intent = Intent(this, IndepPttActivity::class.java)
        intent.putExtra("INDEP_TARGET_ROOM", target)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        streamManager.disconnect()
    }
}