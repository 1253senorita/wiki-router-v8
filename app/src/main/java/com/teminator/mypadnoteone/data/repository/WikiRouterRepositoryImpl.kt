package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.data.datasource.remote.WikiRouterSocketDataSource
import com.teminator.mypadnoteone.domain.repository.WikiRouterRepository
import javax.inject.Inject

class WikiRouterRepositoryImpl @Inject constructor(
    private val socketDataSource: WikiRouterSocketDataSource
) : WikiRouterRepository {

    override fun startRouterConnection(roomKey: String) {
        // 전달받은 roomKey와 함께 임시 사용자 ID 및 피어 ID를 전달하여 소켓 연결
        val defaultUserId = "User_${System.currentTimeMillis()}"
        val defaultPeerId = "Peer_${System.currentTimeMillis()}"
        socketDataSource.connectRouter(roomKey, defaultUserId, defaultPeerId)
    }

    override fun observeIncomingMessages(onResult: (Boolean, String) -> Unit) {
        val mockRawMessage = "New Order: VIP 고객 화물"
        socketDataSource.interceptAndFilterMessage(mockRawMessage) { isAllowed, statusMessage ->
            onResult(isAllowed, statusMessage)
        }
    }

    override fun stopRouterConnection() {
        socketDataSource.disconnectRouter()
    }
}