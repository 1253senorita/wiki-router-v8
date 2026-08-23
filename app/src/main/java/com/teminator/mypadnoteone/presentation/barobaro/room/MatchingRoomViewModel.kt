package com.teminator.mypadnoteone.presentation.barobaro.room

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomLogItem(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String,     // "시스템", "나", "상대방" 등
    val message: String,    // 내용
    val type: LogType       // 타입에 따라 UI 색상 구분 가능
) {
    // 💡 여기에 IMAGE를 추가합니다. 뷰모델 안쪽에는 enum을 선언하지 마세요!
    enum class LogType { SYSTEM, MY_MESSAGE, OTHER_MESSAGE, ACTION, IMAGE }
}

@HiltViewModel
class MatchingRoomViewModel @Inject constructor() : ViewModel() {

    // 현재 방의 연결 및 매칭 상태
    var roomStatus by mutableStateOf("연결 대기 중")
        private set

    // 🚀 로그와 메시지들이 차곡차곡 쌓이는 리스트 상태
    private val _logList = mutableStateListOf<RoomLogItem>()
    val logList: List<RoomLogItem> get() = _logList

    // 🚀 화면에서 입력하던 텍스트 상태를 뷰모델로 이관
    var inputMessage by mutableStateOf("")
        private set

    // 에러 상태 변수
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onInputChanged(newText: String) {
        inputMessage = newText
    }

    /**
     * 세컨드 룸 진입 시 통신 파이프 연결 및 초기 로그 추가
     */
    fun joinMatchingRoom(roomId: String) {
        viewModelScope.launch {
            try {
                roomStatus = "소켓 통신 파이프 연결 시도 중... ($roomId)"
                roomStatus = "통신 연결 완료 (매칭 방 활성화)"

                _logList.add(
                    RoomLogItem(sender = "시스템", message = "[$roomId] 방에 성공적으로 입장했습니다.", type = RoomLogItem.LogType.SYSTEM)
                )
            } catch (e: Exception) {
                errorMessage = "방 접속 실패: ${e.localizedMessage}"
                roomStatus = "연결 실패"
            }
        }
    }

    /**
     * 운행 상태 변경 또는 통신 액션 전송
     */
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

    /**
     * 사용자가 입력한 실시간 대화 메시지 전송 (전송 후 입력창 자동 초기화)
     */
    fun sendCurrentMessage() {
        if (inputMessage.isBlank()) return
        viewModelScope.launch {
            try {
                val messageToSend = inputMessage
                _logList.add(
                    RoomLogItem(sender = "나", message = messageToSend, type = RoomLogItem.LogType.MY_MESSAGE)
                )
                inputMessage = "" // 전송 직후 뷰모델에서 클리어
            } catch (e: Exception) {
                errorMessage = "메시지 전송 실패: ${e.localizedMessage}"
            }
        }
    }

    // 💡 이미지 전송 메시지 로그 추가 함수
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
}