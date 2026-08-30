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
    enum class LogType { SYSTEM, MY_MESSAGE, OTHER_MESSAGE, ACTION, IMAGE, CONTROL_MATRIX }
}

@HiltViewModel
class MatchingRoomViewModel @Inject constructor() : ViewModel() {

    // 💡 [신규 추가] 대화방 참여 인원 수 상태 변수
    var roomUserCount by mutableStateOf(0)
        private set

    fun updateRoomUserCount(count: Int) {
        roomUserCount = count
    }

    // 상대방 통신 ID 상태 변수
    var targetPeerId by mutableStateOf("")
        private set

    fun updateTargetPeerId(newId: String) {
        targetPeerId = newId
    }

    // 현재 방의 연결 및 매칭 상태
    var roomStatus by mutableStateOf("연결 대기 중")
        private set

    // 로그와 메시지들이 차곡차곡 쌓이는 리스트 상태
    private val _logList = mutableStateListOf<RoomLogItem>()
    val logList: List<RoomLogItem> get() = _logList

    // 화면에서 입력하던 텍스트 상태를 뷰모델로 이관
    var inputMessage by mutableStateOf("")
        private set

    // 사용자가 지정한 커스텀 가상 번호 (텍스트 ID 등)
    var virtualNumber by mutableStateOf("VIRTUAL_USER_999")
        private set

    // 에러 상태 변수
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // 🛠️ [공구박스 추가] 마스터 관제 제어를 위한 현재 단계 상태 (1~10단계)
    var currentMatrixStep by mutableStateOf(1)
        private set

    // 🛠️ [공구박스 추가] 단계 변경 함수 (망치로 뚝딱뚝딱 단계를 조절하는 로직)
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

    /**
     * 세컨드 룸 진입 시 통신 파이프 연결 및 초기 로그 추가
     */
    fun joinMatchingRoom(roomId: String) {
        viewModelScope.launch {
            try {
                roomStatus = "소켓 통신 파이프 연결 시도 중... ($roomId)"
                roomStatus = "통신 연결 완료 (매칭 방 활성화)"

                // 💡 방 입장 시 기본 대화방 인원수 1명 설정
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

    /**
     * 웹 통신 / 가상 통신 연결 시뮬레이션 함수
     */
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

                // 💡 통신 연결 성공 시 파트너가 들어온 상태로 가정하여 인원수 2명으로 확장 가능
                roomUserCount = 2
            } catch (e: Exception) {
                errorMessage = "웹 통신 연결 실패: ${e.localizedMessage}"
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
}