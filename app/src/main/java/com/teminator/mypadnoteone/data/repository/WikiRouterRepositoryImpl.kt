package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.data.datasource.remote.WikiRouterSocketDataSource
import com.terminator.mypadnoteone.domain.repository.WikiRouterRepository // ⭐ 이 임포트가 있어야 합니다!
import javax.inject.Inject

// ⭐ 1. : WikiRouterRepository 상속 추가!
// ⭐ 2. @Inject constructor(private val socketDataSource: WikiRouterSocketDataSource) 추가!
class WikiRouterRepositoryImpl @Inject constructor(
    private val socketDataSource: WikiRouterSocketDataSource
) : WikiRouterRepository {

    override fun startRouterConnection(roomId: String) {
        socketDataSource.connectRouter(roomId)
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