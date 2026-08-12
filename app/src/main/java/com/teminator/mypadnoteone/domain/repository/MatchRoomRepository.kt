package com.teminator.mypadnoteone.domain.repository

import com.terminator.mypadnoteone.domain.model.MatchRoom
import kotlinx.coroutines.flow.Flow

interface MatchRoomRepository {
    // 1. 오더 수락 시 새로운 세컨드 룸(매칭 방) 생성 및 서버/로컬 저장
    suspend fun createMatchRoom(orderId: String, shipperId: String, driverId: String): Result<MatchRoom>

    // 2. 특정 방의 상태나 정보를 실시간으로 관찰(Flow)하기 위한 메서드
    fun getMatchRoomStream(roomId: String): Flow<MatchRoom?>

    // 3. 매칭 상태 변경 (예: 수락됨, 운행중, 완료 등)
    suspend fun updateRoomStatus(roomId: String, status: String): Result<Unit>

    // 4. 통신 파이프 해제 및 방 나가기/닫기
    suspend fun closeMatchRoom(roomId: String): Result<Unit>
}