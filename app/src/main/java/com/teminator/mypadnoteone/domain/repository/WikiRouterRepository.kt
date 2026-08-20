package com.terminator.mypadnoteone.domain.repository

interface WikiRouterRepository {
    // 💡 roomKey를 이용해 무전기 통로 접속
    fun startRouterConnection(roomKey: String)
    fun observeIncomingMessages(onResult: (Boolean, String) -> Unit)
    fun stopRouterConnection()
}