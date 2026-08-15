package com.terminator.mypadnoteone.domain.repository

interface WikiRouterRepository {
    fun startRouterConnection(roomId: String)
    fun observeIncomingMessages(onResult: (Boolean, String) -> Unit)
    fun stopRouterConnection()
}