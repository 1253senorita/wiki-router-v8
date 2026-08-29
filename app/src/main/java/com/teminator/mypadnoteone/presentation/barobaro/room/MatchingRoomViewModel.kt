package com.teminator.mypadnoteone.presentation.barobaro.room

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class RoomLogItem(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String,     // "시스템", "나", "상대방" 등
    val message: String,    // 내용
    val type: LogType       // 타입에 따라 UI 색상 구분 가능
) {
    enum class LogType { SYSTEM, MY_MESSAGE, OTHER_MESSAGE, ACTION, IMAGE }
}

@HiltViewModel
class MatchingRoomViewModel @Inject constructor() : ViewModel() {

    private var mSocket: Socket? = null

    // 현재 방의 연결 및 매칭 상태
    var roomStatus by mutableStateOf("연결 대기 중")
        private set

    private val _logList = mutableStateListOf<RoomLogItem>()
    val logList: List<RoomLogItem> get() = _logList

    var inputMessage by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onInputChanged(newText: String) {
        inputMessage = newText
    }

    /**
     * 🚀 세컨드 룸 진입 시 Node.js Socket.io 서버와 연결 및 이벤트 바인딩
     */
    fun joinMatchingRoom(roomId: String, userId: String = "Driver_${System.currentTimeMillis() % 1000}") {
        viewModelScope.launch {
            try {
                roomStatus = "소켓 통신 파이프 연결 시도 중... ($roomId)"

                // 1. Socket.io 서버 연결 (에뮬레이터 기준: http://10.0.2.2:3000)
                // 실제 기기 테스트 시 PC의 IP 주소로 변경 (예: http://192.168.0.5:3000)
                val options = IO.Options().apply {
                    forceNew = true
                }
                mSocket = IO.socket("http://10.0.2.2:3000", options)

                // 2. 소켓 이벤트 리스너 등록
                mSocket?.on(Socket.EVENT_CONNECT) {
                    roomStatus = "통신 연결 완료 (매칭 방 활성화)"
                    Log.d("SocketIO", "Connected to server")

                    // 서버로 방 입장 신호 전송
                    val data = JSONObject().apply {
                        put("roomId", roomId)
                        put("userId", userId)
                        put("peerId", "peer_${System.currentTimeMillis()}")
                    }
                    mSocket?.emit("join-room", data)
                }

                // 서버로부터 접속자 명단 수신
                mSocket?.on("update-user-list") { args ->
                    if (args.isNotEmpty()) {
                        Log.d("SocketIO", "User list updated: ${args[0]}")
                    }
                }

                // 로그 초기화 알림 수신
                mSocket?.on("logs-cleared-notification") { args ->
                    if (args.isNotEmpty()) {
                        val data = args[0] as? JSONObject
                        val by = data?.optString("by") ?: "Admin"
                        _logList.add(
                            RoomLogItem(sender = "시스템", message = "[$by] 님이 로그를 초기화했습니다.", type = RoomLogItem.LogType.SYSTEM)
                        )
                    }
                }

                mSocket?.connect()

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
            _logList.add(
                RoomLogItem(sender = "시스템", message = "상태 업데이트 전송됨: $actionType", type = RoomLogItem.LogType.ACTION)
            )
        }
    }

    /**
     * 사용자가 입력한 실시간 대화 메시지 전송
     */
    fun sendCurrentMessage() {
        if (inputMessage.isBlank()) return
        viewModelScope.launch {
            val messageToSend = inputMessage
            _logList.add(
                RoomLogItem(sender = "나", message = messageToSend, type = RoomLogItem.LogType.MY_MESSAGE)
            )
            inputMessage = ""
        }
    }

    fun sendImageMessage(imageUri: String) {
        viewModelScope.launch {
            _logList.add(
                RoomLogItem(
                    sender = "나",
                    message = "[사진 첨부됨: $imageUri]",
                    type = RoomLogItem.LogType.IMAGE
                )
            )
        }
    }

    fun clearError() {
        errorMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        // 뷰모델 소멸 시 소켓 연결 해제
        mSocket?.disconnect()
        mSocket?.off()
    }
}