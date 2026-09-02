package com.teminator.mypadnoteone.domain.repository

interface WikiRouterRepository {
    fun startRouterConnection(roomKey: String)
    fun observeIncomingMessages(onResult: (Boolean, String) -> Unit)
    // 📌 더미 음성/영상 및 오더 데이터를 저장소를 통해 처리하는 메서드 추가
    suspend fun handleIncomingAudioData(roomId: String, senderId: String, audioBlob: Any)
    fun stopRouterConnection()
}