package com.teminator.mypadnoteone.presentation.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminator.mypadnoteone.domain.usecase.WikiInterceptAndHoldOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val interceptAndHoldOrderUseCase: WikiInterceptAndHoldOrderUseCase // ⭐ AI 보미 유즈케이스 주입!
) : ViewModel() {

    // 고객이 요청한 내역이나 상태를 관리할 수 있는 변수
    var clientStatusText by mutableStateOf("고객님, 환영합니다. -- AI-보미 시스템 대기 중...")
        private set

    init {
        // 뷰모델이 생성되자마자 AI 보미의 인터셉트 통로를 가동합니다!
        startListeningToBomiAgent()
    }

    private fun startListeningToBomiAgent() {
        viewModelScope.launch {
            // 유즈케이스를 통해 위키 라우터 메시지 및 보미의 필터링 결과 관찰
            interceptAndHoldOrderUseCase { isAllowed, statusMessage ->
                // AI 보미가 판별한 상태 메시지를 UI 상태에 실시간 반영
                clientStatusText = if (isAllowed) {
                    "[하이패스 통과] $statusMessage"
                } else {
                    "[보미 홀드 중] $statusMessage"
                }
            }
        }
    }

    fun updateStatus(newText: String) {
        clientStatusText = newText
    }
}