package com.terminator.mypadnoteone.domain.model

data class MatchRoom(
    val roomId: String,          // 세컨드 룸 고유 ID
    val orderId: String,         // 연동된 오더 ID
    val shipperId: String,       // 화주(등록자) ID
    val driverId: String?,       // 수락한 기사 ID (초기엔 null 가능)
    val status: String,          // 매칭 상태 ("WAITING", "ACCEPTED", "COMPLETED")
    val lastMessage: String?     // 방 안에서 주고받은 마지막 메시지나 상태 요약
)