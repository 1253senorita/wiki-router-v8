package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.data.datasource.remote.WikiRouterSocketDataSource
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import com.teminator.mypadnoteone.domain.repository.WikiRouterRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class WikiRouterRepositoryImpl @Inject constructor(
    private val socketDataSource: WikiRouterSocketDataSource,
    private val baroBaroRepository: BaroBaroRepository // 💡 로컬/메모리 저장소 주입
) : WikiRouterRepository {

    override fun startRouterConnection(roomKey: String) {
        val defaultUserId = "User_${System.currentTimeMillis()}"
        val defaultPeerId = "Peer_${System.currentTimeMillis()}"
        socketDataSource.connectRouter(roomKey, defaultUserId, defaultPeerId)
    }

    override fun observeIncomingMessages(onResult: (Boolean, String) -> Unit) {
        // 실제 소켓 이벤트 리스너와 연동될 수 있는 mock 또는 실제 수신부
        val mockRawMessage = "New Order: VIP 고객 화물"
        socketDataSource.interceptAndFilterMessage(mockRawMessage) { isAllowed, statusMessage ->
            if (isAllowed) {
                // 🎯 VIP 단골 콜인 경우, BaroBaroRepository에 더미 오더 자동 생성 후 주입 예시
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val newOrder = DispatchOrder(
                        id = "Order_${System.currentTimeMillis()}",
                        status = "PENDING",
                        driverId = "",
                        // 기타 필드...
                    )
                    baroBaroRepository.addOrder(newOrder)
                }
            }
            onResult(isAllowed, statusMessage)
        }
    }

    override suspend fun handleIncomingAudioData(roomId: String, senderId: String, audioBlob: Any) {
        // 1. 소켓을 통해 어태치 저장소(또는 캐시 디렉토리)에 음성/영상 파일 기록
        socketDataSource.sendAudioSync(roomId, senderId, audioBlob)

        // 2. 필요한 경우 로컬 저장소 상태 갱신
        // val dummyOrder = DispatchOrder(...)
        // baroBaroRepository.addOrder(dummyOrder)
    }

    override fun stopRouterConnection() {
        socketDataSource.disconnectRouter()
    }
}