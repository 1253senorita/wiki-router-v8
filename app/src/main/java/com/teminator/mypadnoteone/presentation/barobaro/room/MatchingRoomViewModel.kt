package com.terminator.mypadnoteone.presentation.barobaro.room

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchingRoomViewModel @Inject constructor(
    // 추후 MatchRoomRepository나 소켓 매니저를 여기에 주입받아 사용합니다.
    // private val matchRoomRepository: MatchRoomRepository,
    // private val aeroSocketManager: AeroSocketManager
) : ViewModel() {

    // 현재 방의 연결 및 매칭 상태 (예: "대기중", "연결됨", "운행중", "완료")
    var roomStatus by mutableStateOf("연결 대기 중")
        private set

    // 방 내부에서 주고받는 마지막 상태 메시지나 로그
    var lastLogMessage by mutableStateOf<String?>(null)
        private set

    // 에러 상태 변수
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * 세컨드 룸 진입 시 통신 파이프(소켓 등)를 연결하고 방 데이터를 구독하는 함수
     */
    fun joinMatchingRoom(roomId: String) {
        viewModelScope.launch {
            try {
                roomStatus = "소켓 통신 파이프 연결 시도 중... ($roomId)"

                // TODO: aeroSocketManager 또는 repository를 통한 방 접속 로직 구현
                // aeroSocketManager.connectToRoom(roomId)

                roomStatus = "통신 연결 완료 (매칭 방 활성화)"
            } catch (e: Exception) {
                errorMessage = "방 접속 실패: ${e.localizedMessage}"
                roomStatus = "연결 실패"
            }
        }
    }

    /**
     * 운행 상태 변경 또는 통신 메시지 전송 이벤트 함수
     */
    fun updateRoomAction(actionType: String) {
        viewModelScope.launch {
            try {
                // TODO: 서버나 상대방에게 상태 변경 패킷 전송
                lastLogMessage = "상태 업데이트 전송됨: $actionType"
            } catch (e: Exception) {
                errorMessage = "업데이트 실패: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}