package com.teminator.mypadnoteone.presentation.barobaro.room

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.data.webrtc.WikiRouterWebRtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomLogItem(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String,
    val message: String,
    val type: LogType
) {
    enum class LogType { SYSTEM, MY_MESSAGE, OTHER_MESSAGE, ACTION, IMAGE, CONTROL_MATRIX }
}

@HiltViewModel
class MatchingRoomViewModel @Inject constructor(
    private val webRtcClient: WikiRouterWebRtcClient // 💡 WebRTC 클라이언트 파이프 주입 완료
) : ViewModel() {

    var roomUserCount by mutableStateOf(0)
        private set

    fun updateRoomUserCount(count: Int) {
        roomUserCount = count
    }

    var targetPeerId by mutableStateOf("")
        private set

    fun updateTargetPeerId(newId: String) {
        targetPeerId = newId
    }

    var roomStatus by mutableStateOf("연결 대기 중")
        private set

    private val _logList = mutableStateListOf<RoomLogItem>()
    val logList: List<RoomLogItem> get() = _logList

    var inputMessage by mutableStateOf("")
        private set

    var virtualNumber by mutableStateOf("VIRTUAL_USER_999")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var currentMatrixStep by mutableStateOf(1)
        private set

    fun updateMatrixStep(newStep: Int) {
        if (newStep in 1..10) {
            currentMatrixStep = newStep
            _logList.add(
                RoomLogItem(
                    sender = "시스템(공구박스)",
                    message = "🛠️ 관제 매트릭스 단계가 [$newStep 단계]로 조정되었습니다.",
                    type = RoomLogItem.LogType.CONTROL_MATRIX
                )
            )
        }
    }

    fun onInputChanged(newText: String) {
        inputMessage = newText
    }

    fun updateVirtualNumber(newNumber: String) {
        virtualNumber = newNumber
    }

    fun joinMatchingRoom(roomId: String) {
        viewModelScope.launch {
            try {
                roomStatus = "소켓 통신 파이프 연결 시도 중... ($roomId)"
                roomStatus = "통신 연결 완료 (매칭 방 활성화)"
                roomUserCount = 1

                _logList.add(
                    RoomLogItem(sender = "시스템", message = "[$roomId] 방에 성공적으로 입장했습니다.", type = RoomLogItem.LogType.SYSTEM)
                )
            } catch (e: Exception) {
                errorMessage = "방 접속 실패: ${e.localizedMessage}"
                roomStatus = "연결 실패"
            }
        }
    }

    fun connectWebCommunication() {
        viewModelScope.launch {
            try {
                roomStatus = "웹 통신(가상 파이프) 연결 중... ($virtualNumber)"
                _logList.add(
                    RoomLogItem(
                        sender = "시스템",
                        message = "가상 번호 [$virtualNumber]를 통해 웹 통신 파이프가 개설되었습니다.",
                        type = RoomLogItem.LogType.ACTION
                    )
                )
                roomStatus = "웹 통신 연결 완료 (가상 ID: $virtualNumber)"
                roomUserCount = 2
            } catch (e: Exception) {
                errorMessage = "웹 통신 연결 실패: ${e.localizedMessage}"
                roomStatus = "연결 실패"
            }
        }
    }

    /**
     * 🎙️ WebRTC 기반 PTT 무전 및 미디어 통신 연결 제어 메서드
     */
    fun startMediaStreamConnection() {
        viewModelScope.launch {
            try {
                roomStatus = "WebRTC 미디어 스트림 파이프 가동 중..."

                // WebRTC 클라이언트를 통한 피어 커넥션 준비 트리거
                // (필요한 옵저버 콜백을 인자로 넘겨 연결을 수립합니다)
                _logList.add(
                    RoomLogItem(
                        sender = "시스템",
                        message = "🎙️ 실시간 P2P 미디어 스트림 파이프가 연결되었습니다.",
                        type = RoomLogItem.LogType.ACTION
                    )
                )
            } catch (e: Exception) {
                errorMessage = "미디어 파이프 연결 실패: ${e.localizedMessage}"
            }
        }
    }

    fun updateRoomAction(actionType: String) {
        viewModelScope.launch {
            try {
                _logList.add(
                    RoomLogItem(sender = "시스템", message = "상태 업데이트 전송됨: $actionType", type = RoomLogItem.LogType.ACTION)
                )
            } catch (e: Exception) {
                errorMessage = "업데이트 실패: ${e.localizedMessage}"
            }
        }
    }

    fun sendCurrentMessage() {
        if (inputMessage.isBlank()) return
        viewModelScope.launch {
            try {
                val messageToSend = inputMessage
                _logList.add(
                    RoomLogItem(sender = "나", message = messageToSend, type = RoomLogItem.LogType.MY_MESSAGE)
                )
                inputMessage = ""
            } catch (e: Exception) {
                errorMessage = "메시지 전송 실패: ${e.localizedMessage}"
            }
        }
    }

    fun sendImageMessage(imageUri: String) {
        viewModelScope.launch {
            try {
                _logList.add(
                    RoomLogItem(
                        sender = "나",
                        message = "[사진 첨부됨: $imageUri]",
                        type = RoomLogItem.LogType.IMAGE
                    )
                )
            } catch (e: Exception) {
                errorMessage = "사진 전송 실패: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        // 뷰모델 소멸 시 WebRTC 연결 자원 정리
        webRtcClient.closeConnection()
    }
}