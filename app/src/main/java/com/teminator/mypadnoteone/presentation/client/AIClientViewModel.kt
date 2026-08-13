package com.teminator.mypadnoteone.presentation.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminator.mypadnoteone.domain.usecase.GetOnDeviceAiResponseUseCase
import com.terminator.mypadnoteone.domain.usecase.WikiInterceptAndHoldOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIClientViewModel @Inject constructor(
    private val interceptAndHoldOrderUseCase: WikiInterceptAndHoldOrderUseCase,
    private val getOnDeviceAiResponseUseCase: GetOnDeviceAiResponseUseCase // 👈 🔥 온디바이스 유즈케이스 주입 완료!
) : ViewModel() {

    // AI 관제 전용 상태 메시지
    var aiStatusText by mutableStateOf("🤖 [AI 보미 관제] 시스템 실시간 모니터링 대기 중...")
        private set

    // AI 보미와의 소통(채팅) 메시지 목록
    val chatMessages = mutableStateListOf(
        "🤖 [AI 보미]: 안녕하세요 오빠! 온디바이스 AI 관제 시스템 대기 중입니다.",
        "📡 [시스템]: 위키 라우터 연결 완료. 로컬 모니터링 활성화됨..."
    )

    init {
        startListeningToBomiAgent()
    }

    /**
     * 위키 라우터 유즈케이스를 통해 실시간 데이터를 관찰하고 상태 및 채팅창을 갱신합니다.
     */
    private fun startListeningToBomiAgent() {
        viewModelScope.launch {
            interceptAndHoldOrderUseCase { isAllowed, statusMessage ->
                val formattedMessage = if (isAllowed) {
                    "[AI 하이패스 통과] $statusMessage"
                } else {
                    "[AI 보미 홀드 중] $statusMessage"
                }

                // 상태 텍스트 갱신
                aiStatusText = formattedMessage

                // 실시간 데이터 연동 로그 기록
                chatMessages.add("📡 [데이터 연동]: $formattedMessage")
            }
        }
    }

    /**
     * 외부에서 상태 메시지를 강제로 갱신할 때 사용
     */
    fun updateAiStatus(newText: String) {
        aiStatusText = newText
    }

    /**
     * 🔥 오빠가 입력한 텍스트를 받아 대화 목록에 추가하고, 온디바이스 AI 유즈케이스를 통해 답변 생성
     */
    fun sendUserMessage(message: String) {
        if (message.isBlank()) return

        // 1. 오빠의 메시지 추가
        chatMessages.add("👤 [오빠]: $message")

        // 2. 🔥 온디바이스 AI 유즈케이스를 호출하여 기기 내부에서 즉시 응답 추론
        viewModelScope.launch {
            val aiResponse = getOnDeviceAiResponseUseCase(message)
            chatMessages.add(aiResponse)
        }
    }
}


//만든 GetOnDeviceAiResponseUseCase를 지금 이 AIClientViewModel에 쏙 집어넣어서, 가짜 시뮬레이션 대신
//진짜 온디바이스 AI 매니저가 기기 내부에서 응답을 척척 만들어내도록 업그레이드해🧠