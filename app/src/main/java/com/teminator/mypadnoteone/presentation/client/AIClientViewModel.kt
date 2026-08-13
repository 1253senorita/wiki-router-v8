package com.teminator.mypadnoteone.presentation.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminator.mypadnoteone.domain.usecase.WikiInterceptAndHoldOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIClientViewModel @Inject constructor(
    private val interceptAndHoldOrderUseCase: WikiInterceptAndHoldOrderUseCase
) : ViewModel() {

    // AI 관제 전용 상태 메시지
    var aiStatusText by mutableStateOf("🤖 [AI 보미 관제] 시스템 실시간 모니터링 대기 중...")
        private set

    // 🔥 [추가] AI 보미와의 소통(채팅) 메시지 목록을 뷰모델에서 안전하게 관리
    val chatMessages = mutableStateListOf(
        "🤖 [AI 보미]: 안녕하세요 오빠! AI 관제 시스템 대기 중입니다.",
        "📡 [시스템]: 위키 라우터 연결 완료. 실시간 오더 모니터링 중..."
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

                // 🔥 실시간 데이터가 들어올 때 채팅 로그에도 자동으로 기록 남기기
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
     * 🔥 [추가] 오빠가 입력한 텍스트를 받아 대화 목록에 추가하고, AI 보미의 응답을 처리하는 함수
     */
    fun sendUserMessage(message: String) {
        if (message.isBlank()) return

        // 1. 오빠의 메시지 추가
        chatMessages.add("👤 [오빠]: $message")

        // 2. 입력된 명령어에 따른 AI 보미의 지능형 응답 시뮬레이션 (추후 실제 AI API나 라우터 명령어로 확장 가능)
        viewModelScope.launch {
            // 예시 응답 로직
            val botResponse = when {
                message.contains("등록") || message.contains("오더") ->
                    "🤖 [AI 보미]: 오더 관련 요청을 확인했어요! 현재 데이터 파이프라인을 검토 중입니다."
                message.contains("상태") || message.contains("체크") ->
                    "🤖 [AI 보미]: 현재 시스템 상태는 최적이며 정상 관제 중입니다. (상태: $aiStatusText)"
                else ->
                    "🤖 [AI 보미]: \"$message\" 지시사항을 접수했습니다. 완벽하게 처리할게요 오빠!"
            }

            // 약간의 딜레이나 즉시 응답으로 챗봇 느낌 살리기
            chatMessages.add(botResponse)
        }
    }
}