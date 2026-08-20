package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import com.terminator.mypadnoteone.domain.repository.WikiRouterRepository // 📌 무전기 라우터 레포지토리 임포트
import com.teminator.mypadnoteone.data.cache.OrderCacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaroBaroViewModel @Inject constructor(
    private val repository: BaroBaroRepository,
    private val wikiRouterRepository: WikiRouterRepository // 💡 [추가] 무전기/소켓 라우터 레포지토리 주입
) : ViewModel() {

    // 💡 [메모리 방어] 오더 캐시 매니저 도입 (최대 100개 유지로 OOM 방지)
    private val cacheManager = OrderCacheManager(maxCapacity = 100)

    val orderList: List<DispatchOrder> get() = cacheManager.cachedOrders

    var searchQuery by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    // 💡 최신 등록된 항목이 위로 오도록 역순(Reversed)으로 리스트 정렬 및 필터링 적용
    val filteredOrderList: List<DispatchOrder>
        get() {
            // 1단계: "수락됨" 상태가 아닌(미수락) 오더들만 추려냄
            val unacceptedList = cacheManager.cachedOrders.filter { order ->
                order.status != "수락됨"
            }

            // 2단계: 검색어가 있으면 추가 필터링, 없으면 전체 미수락 리스트 반환
            val list = if (searchQuery.isBlank()) {
                unacceptedList
            } else {
                unacceptedList.filter {
                    it.route.contains(searchQuery, ignoreCase = true) ||
                            it.cargoInfo.contains(searchQuery, ignoreCase = true)
                }
            }
            // 3단계: 최신 등록된 항목이 위로 오도록 역순 정렬
            return list.reversed()
        }

    var selectedOrder by mutableStateOf<DispatchOrder?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var memoryToastMessage by mutableStateOf<String?>(null)
        private set

    var mockMatchingRoomId by mutableStateOf<String?>(null)
        private set

    fun forceCreateTestMatchRoom(orderId: String) {
        viewModelScope.launch {
            try {
                val fakeRoomId = "ROOM_TEST_$orderId"
                mockMatchingRoomId = fakeRoomId
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "가상 방 생성 실패: ${e.localizedMessage}"
            }
        }
    }

    fun clearMockRoomId() {
        mockMatchingRoomId = null
    }

    fun clearMemoryToast() {
        memoryToastMessage = null
    }

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                memoryToastMessage = "⚠️ 초기 오더 데이터 로딩 중..."

                val orders = repository.getOrders()

                if (orders.isEmpty()) {
                    val regions = listOf("서울", "인천", "경기", "부산", "대구", "대전", "광주", "울산", "강원", "충남", "전북", "경남", "제주", "경북", "전남")
                    val cargoTypes = listOf("1톤 다스퀵 / 박스", "2.5톤 윙바디", "5톤 카고 / 파레트", "11톤 윙바디 / 톤백", "5톤 냉동탑차", "25톤 대형 트레일러")
                    val prices = listOf("130,000원", "180,000원", "250,000원", "350,000원", "480,000원", "520,000원", "750,000원")

                    val generatedDummyList = mutableListOf<DispatchOrder>()
                    for (i in 1..10) {
                        val start = regions[(i * 3) % regions.size]
                        val end = regions[(i * 7) % regions.size]
                        val cargo = cargoTypes[i % cargoTypes.size]
                        val price = prices[i % prices.size]
                        val dummyId = "dummy_$i"

                        generatedDummyList.add(
                            DispatchOrder(
                                id = dummyId,
                                roomKey = "room_$dummyId", // 💡 샘플 데이터도 룸 키 자동 생성 부여
                                route = "$start ➔ $end",
                                cargoInfo = cargo,
                                price = price,
                                status = "대기중",
                                description = "기본 테스트용 샘플 오더 데이터입니다 (#$i)"
                            )
                        )
                    }
                    cacheManager.updateOrders(generatedDummyList)
                    memoryToastMessage = "✅ 샘플 오더 10개 캐시 적재 완료 (대기 중)"
                } else {
                    cacheManager.updateOrders(orders)
                    memoryToastMessage = "✅ 원격 서버 데이터 동기화 완료!"
                }
            } catch (e: OutOfMemoryError) {
                memoryToastMessage = "❌ 메모리 부족! 임시 캐시를 정리했습니다."
                cacheManager.clear()
            } catch (e: Exception) {
                errorMessage = "데이터 로드 실패: ${e.localizedMessage}"
            }
        }
    }

    fun selectOrder(order: DispatchOrder?) {
        selectedOrder = order
    }

    fun clearError() {
        errorMessage = null
    }

    fun addOrder(route: String, cargo: String, price: String, description: String) {
        if (route.isBlank() || cargo.isBlank() || price.isBlank()) {
            errorMessage = "필수 항목을 모두 입력해주세요!"
            return
        }

        viewModelScope.launch {
            try {
                val newId = "order_${System.currentTimeMillis()}"
                val newOrder = DispatchOrder(
                    id = newId,
                    roomKey = "room_$newId",
                    route = route,
                    cargoInfo = cargo,
                    price = price,
                    status = "대기중",
                    description = description
                )
                repository.addOrder(newOrder)
                cacheManager.addOrUpdateOrder(newOrder)
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "오더 등록 실패: ${e.localizedMessage}"
            }
        }
    }

    fun updateOrder(orderId: String, route: String, cargo: String, price: String, description: String) {
        if (route.isBlank() || cargo.isBlank() || price.isBlank()) {
            errorMessage = "필수 항목을 모두 입력해주세요!"
            return
        }

        viewModelScope.launch {
            try {
                val existing = cacheManager.cachedOrders.find { it.id == orderId }
                if (existing != null) {
                    val updatedOrder = existing.copy(
                        route = route,
                        cargoInfo = cargo,
                        price = price,
                        description = description
                    )

                    repository.updateOrder(updatedOrder)
                    cacheManager.addOrUpdateOrder(updatedOrder)

                    if (selectedOrder?.id == orderId) {
                        selectedOrder = updatedOrder
                    }
                    errorMessage = null
                } else {
                    errorMessage = "수정할 오더를 찾을 수 없습니다."
                }
            } catch (e: Exception) {
                errorMessage = "오더 수정 실패: ${e.localizedMessage}"
            }
        }
    }

    // 💡 1. 기본 오더 수락 함수
    fun acceptOrder(orderId: String, currentDriverId: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, "수락됨", currentDriverId)

                val existing = cacheManager.cachedOrders.find { it.id == orderId }
                if (existing != null) {
                    val updated = existing.copy(
                        status = "수락됨",
                        driverId = currentDriverId
                    )
                    cacheManager.addOrUpdateOrder(updated)
                    selectedOrder = updated
                }
            } catch (e: Exception) {
                errorMessage = "상태 변경 실패: ${e.localizedMessage}"
            }
        }
    }

    // 💡 2. 오더 수락 및 무전기 라우터 연동 통합 함수
    fun acceptOrderAndConnectRouter(orderId: String, currentDriverId: String, roomKey: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, "수락됨", currentDriverId)

                val existing = cacheManager.cachedOrders.find { it.id == orderId }
                if (existing != null) {
                    val updated = existing.copy(
                        status = "수락됨",
                        driverId = currentDriverId
                    )
                    cacheManager.addOrUpdateOrder(updated)
                    selectedOrder = updated
                }

                // 📌 확보된 roomKey를 이용해 WikiRouter 무전기 채널 가동!
                wikiRouterRepository.startRouterConnection(roomKey)

                // 📌 AI 보미의 실시간 메시지 옵저브 시작
                wikiRouterRepository.observeIncomingMessages { isAllowed, statusMessage ->
                    memoryToastMessage = statusMessage
                }

            } catch (e: Exception) {
                errorMessage = "오더 수락 및 라우터 연동 실패: ${e.localizedMessage}"
            }
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            try {
                repository.deleteOrder(orderId)
                cacheManager.removeOrder(orderId)

                if (selectedOrder?.id == orderId) {
                    selectedOrder = null
                }
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "오더 삭제 실패: ${e.localizedMessage}"
            }
        }
    }
}