package com.teminator.mypadnoteone.indep

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.databinding.ActivityAerorouterEntryBinding
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomViewModel


// 💡 프로젝트에 맞는 테마 임포트로 변경 (예: com.teminator.mypadnoteone.ui.theme 등 확인 필요)
// 만약 테마 임포트가 헷갈린다면 아래처럼 액티비티 내부에서 XML 레이아웃과 Compose를 분리하는 것이 안전합니다.

class IndepRouterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAerorouterEntryBinding
    private lateinit var streamManager: IndepStreamManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAerorouterEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        streamManager = IndepStreamManager()

        streamManager.connect(
            onConnected = {
                runOnUiThread {
                    Toast.makeText(this, "서버 소켓 연결 성공!", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { err ->
                runOnUiThread {
                    Toast.makeText(this, "서버 소켓 연결 실패: $err", Toast.LENGTH_SHORT).show()
                }
            }
        )

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnAuthJoin.setOnClickListener {
            val modeId = binding.etModeOrRoom.text.toString().trim().ifEmpty { "INDEP_MASTER" }
            Toast.makeText(this, "[$modeId] 독립 권한 모드 임시 허용 통과!", Toast.LENGTH_SHORT).show()
            moveToMatchingRoom(modeId)
        }

        binding.btnRoomJoin.setOnClickListener {
            val roomId = binding.etModeOrRoom.text.toString().trim().ifEmpty { "INDEP_ROOM" }
            streamManager.joinRoom(roomId)
            Toast.makeText(this, "[$roomId] 독립 방으로 입장합니다.", Toast.LENGTH_SHORT).show()
            moveToMatchingRoom(roomId)
        }
    }

    // 💡 기존처럼 액티비티 전환 방식으로 안전하게 연결하려면 Intent를 쓰거나,
    // 혹은 별도의 Compose 전용 Activity를 만드는 것이 좋습니다.
    private fun moveToMatchingRoom(target: String) {
        Toast.makeText(this, "[$target] 아지트 방으로 진입합니다.", Toast.LENGTH_SHORT).show()

        // 💡 XML 액티비티에서 컴포즈를 덮어씌울 때 사용하는 정확한 문법입니다.
        setContent {
            MaterialTheme {
                val viewModel: MatchingRoomViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                MatchingRoomScreen(
                    roomId = target,
                    order = null,
                    viewModel = viewModel,
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        streamManager.disconnect()
    }
}