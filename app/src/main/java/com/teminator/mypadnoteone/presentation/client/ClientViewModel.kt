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
    private val interceptAndHoldOrderUseCase: WikiInterceptAndHoldOrderUseCase
) : ViewModel() {

    // 일반 고객 페이지 전용 상태 메시지
    var clientStatusText by mutableStateOf("📦 [고객 대시보드] 환영합니다. 오더 요청 대기 중...")
        private set

    init {
        startListeningToClientOrders()
    }

    private fun startListeningToClientOrders() {
        viewModelScope.launch {
            interceptAndHoldOrderUseCase { isAllowed, statusMessage ->
                clientStatusText = if (isAllowed) {
                    "[고객 승인됨] $statusMessage"
                } else {
                    "[고객 대기/보류] $statusMessage"
                }
            }
        }
    }

    fun updateClientStatus(newText: String) {
        clientStatusText = newText
    }
}