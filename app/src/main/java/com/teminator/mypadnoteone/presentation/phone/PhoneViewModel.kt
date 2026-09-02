package com.teminator.mypadnoteone.presentation.phone

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.data.webrtc.WikiRouterWebRtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneViewModel @Inject constructor(
    private val webRtcClient: WikiRouterWebRtcClient // 💡 Hilt를 통한 WebRTC 클라이언트 주입
) : ViewModel() {

    var dialNumberInput by mutableStateOf("")
        private set

    var callStatusMessage by mutableStateOf("대기 중")
        private set

    fun updateDialNumber(number: String) {
        dialNumberInput = number
    }

    /**
     * 📞 전화 다이얼 통화 및 라우터 소켓 파이프 연결 요청
     */
    fun startPhoneCall(targetNumber: String) {
        if (targetNumber.isBlank()) return

        viewModelScope.launch {
            try {
                callStatusMessage = "[$targetNumber] 연결 시도 중..."

                // WebRTC 클라이언트나 라우터 소켓 연동 트리거 지점
                // webRtcClient를 활용한 P2P 채널 오픈 또는 시그널링 호출

                callStatusMessage = "[$targetNumber] 통화 연결 완료 (WebRTC 활성화)"
            } catch (e: Exception) {
                callStatusMessage = "통화 연결 실패: ${e.localizedMessage}"
            }
        }
    }

    fun terminateCall() {
        viewModelScope.launch {
            callStatusMessage = "통화 종료 중..."
            webRtcClient.closeConnection()
            callStatusMessage = "통화가 종료되었습니다."
        }
    }

    override fun onCleared() {
        super.onCleared()
        webRtcClient.closeConnection()
    }
}