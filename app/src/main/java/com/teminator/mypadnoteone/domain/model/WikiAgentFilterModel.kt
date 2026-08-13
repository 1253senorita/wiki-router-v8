package com.terminator.mypadnoteone.domain.model

data class WikiAgentFilterModel(
    val isAllowed: Boolean,       // VIP/단골 여부 (하이패스 통과 여부)
    val statusMessage: String,    // AI 보미의 상담 및 홀드 메시지
    val rawPayload: String        // 원본 수신 데이터
)