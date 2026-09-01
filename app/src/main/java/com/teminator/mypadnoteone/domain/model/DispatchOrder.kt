package com.teminator.mypadnoteone.domain.model

data class DispatchOrder(
    val id: String = "",          // 파이어베이스 문서 ID
    val roomKey: String = "",     // 실시간 무전기 통신용 룸 키
    val route: String = "",       // 경로
    val cargoInfo: String = "",   // 화물 정보
    val price: String = "",       // 금액
    val status: String = "대기중", // 상태 (대기중 / 수락됨 등)
    val description: String = "", // 설명
    val driverId: String = "",    // 이 오더를 잡은 기사(클라이언트)의 고유 ID
    val shipperPhone: String = "", // 📞 화주 전화번호

    // ⏰ [추가] 예약 배차 및 스누즈/시간 제어 관련 필드
    val isReserved: Boolean = false,          // 예약 오더 여부 (true: 예약, false: 즉시)
    val reservationTime: String = "",         // 예약 일시 (예: "2026-09-02 14:00")
    val notificationOption: String = "정시 울림"  // 알림 상태 및 스누즈 옵션 (예: "10분 전", "30분 연기" 등)
)