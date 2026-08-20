package com.teminator.mypadnoteone.domain.model

data class DispatchOrder(
    val id: String = "",          // 파이어베이스 문서 ID
    val roomKey: String = "",     // 실시간 무전기 통신용 룸 키
    val route: String = "",       // 경로
    val cargoInfo: String = "",   // 화물 정보
    val price: String = "",       // 금액
    val status: String = "대기중", // 상태 (대기중 / 수락됨 등)
    val description: String = "", // 설명
    val driverId: String = ""     // 📌 [추가 추천] 이 오더를 잡은 기사(클라이언트)의 고유 ID
)