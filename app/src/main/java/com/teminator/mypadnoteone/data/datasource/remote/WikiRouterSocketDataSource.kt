package com.teminator.mypadnoteone.data.datasource.remote

import android.util.Log

object WikiRouterSocketDataSource {

    private const val TAG = "WikiRouterSocket"
    private var isConnected: Boolean = false

    // 소켓 연결 시뮬레이션 또는 초기화
    fun connectRouter(roomId: String) {
        // TODO: Socket.io 또는 PeerJS 연결 로직 구현
        isConnected = true
        Log.d(TAG, "WIKI-ROUTER 프라이빗 룸 [$roomId] 연결 성공!")
    }

    // 🎯 AI '보미'의 핵심 후킹(Hooking) 메서드
    // 서버나 상대방으로부터 데이터가 들어왔을 때, UI에 곧바로 쏘기 전에 AI가 먼저 가로채서 분석합니다.
    fun interceptAndFilterMessage(rawMessage: String, onBomiFiltered: (Boolean, String) -> Unit) {
        Log.d(TAG, "AI 보미 인터셉트 작동: $rawMessage")

        // 1차 필터링 로직 (예: 스팸 콜, 불필요한 단가 필터링, 단골 여부 체크)
        val isTrustedCustomer = rawMessage.contains("VIP") || rawMessage.contains("단골")

        if (isTrustedCustomer) {
            // 단골이거나 검증된 콜인 경우: 하이패스 통과 (즉시 연결)
            onBomiFiltered(true, "[보미 비서] 단골 고객 오der입니다. 즉시 연결합니다!")
        } else {
            // 일반 콜인 경우: AI 보미가 1차 상담 및 임시 홀드 처리
            onBomiFiltered(false, "[보미 비서] 1차 상담 진행 중: 임시 홀드(Tentative Hold)를 겁니다.")
        }
    }

    // 소켓 연결 해제
    fun disconnectRouter() {
        isConnected = false
        Log.d(TAG, "WIKI-ROUTER 연결 해제")
    }
}