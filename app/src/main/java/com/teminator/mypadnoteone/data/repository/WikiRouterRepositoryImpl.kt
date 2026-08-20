package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.data.datasource.remote.WikiRouterSocketDataSource
import com.terminator.mypadnoteone.domain.repository.WikiRouterRepository
import javax.inject.Inject

class WikiRouterRepositoryImpl @Inject constructor(
    private val socketDataSource: WikiRouterSocketDataSource
) : WikiRouterRepository {

    override fun startRouterConnection(roomKey: String) {
        // 전달받은 roomKey로 소켓 데이터 소스 연결
        socketDataSource.connectRouter(roomKey)
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