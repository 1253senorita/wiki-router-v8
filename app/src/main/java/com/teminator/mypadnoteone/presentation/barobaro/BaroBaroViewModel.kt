package com.teminator.mypadnoteone.presentation.barobaro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.data.cache.OrderCacheManager
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import com.teminator.mypadnoteone.domain.repository.WikiRouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaroBaroViewModel @Inject constructor(
    private val repository: BaroBaroRepository,
    private val wikiRouterRepository: WikiRouterRepository
) : ViewModel() {

    private val cacheManager = OrderCacheManager(maxCapacity = 100)

    val orderList: List<DispatchOrder> get() = cacheManager.cachedOrders

    var searchQuery by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    val filteredOrderList: List<DispatchOrder>
        get() {
            val unacceptedList = cacheManager.cachedOrders.filter { order ->
                order.status != "수락됨"
            }

            val list = if (searchQuery.isBlank()) {
                unacceptedList
            } else {
                unacceptedList.filter {
                    it.route.contains(searchQuery, ignoreCase = true) ||
                            it.cargoInfo.contains(searchQuery, ignoreCase = true)
                }
            }
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
                                roomKey = "room_$dummyId",
                                route = "$start ➔ $end",
                                cargoInfo = cargo,
                                price = price,
                                status = "대기중",
                                description = "기본 테스트용 샘플 오더 데이터입니다 (#$i)",
                                shipperPhone = "010-1234-56${i % 10}",
                                isReserved = (i % 3 == 0), // 3개 중 하나는 샘플 예약 오더로 지정
                                reservationTime = if (i % 3 == 0) "2026-09-03 14:00" else "",
                                notificationOption = "정시 울림"
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

    // 💡 [확장] 예약 관련 필드(isReserved, reservationTime, notificationOption)가 포함된 addOrder
    fun addOrder(
        route: String,
        cargo: String,
        price: String,
        description: String,
        shipperPhone: String,
        isReserved: Boolean = false,
        reservationTime: String = "",
        notificationOption: String = "정시 울림"
    ) {
        if (route.isBlank() || cargo.isBlank() || price.isBlank()) {
            errorMessage = "필수 항목을 모두 입력해주세요!"
            return
        }
        if (isReserved && reservationTime.isBlank()) {
            errorMessage = "예약 오더인 경우 예약 일시를 입력해주세요!"
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
                    description = description,
                    shipperPhone = shipperPhone,
                    isReserved = isReserved,
                    reservationTime = reservationTime,
                    notificationOption = notificationOption
                )
                repository.addOrder(newOrder)
                cacheManager.addOrUpdateOrder(newOrder)
                errorMessage = null
                memoryToastMessage = if (isReserved) "⏰ 예약 오더가 등록되었습니다." else "⚡ 즉시 오더가 등록되었습니다."
            } catch (e: Exception) {
                errorMessage = "오더 등록 실패: ${e.localizedMessage}"
            }
        }
    }

    // 💡 [확장] 예약 관련 필드가 포함된 updateOrder
    fun updateOrder(
        orderId: String,
        route: String,
        cargo: String,
        price: String,
        description: String,
        shipperPhone: String,
        isReserved: Boolean = false,
        reservationTime: String = "",
        notificationOption: String = "정시 울림"
    ) {
        if (route.isBlank() || cargo.isBlank() || price.isBlank()) {
            errorMessage = "필수 항목을 모두 입력해주세요!"
            return
        }

        viewModelScope.launch {
            try {
                // 💡 수정 후 (올바른 코드)
                val existing = cacheManager.cachedOrders.find { it.id == orderId }
                if (existing != null) {
                    val updatedOrder = existing.copy(
                        route = route,
                        cargoInfo = cargo,
                        price = price,
                        description = description,
                        shipperPhone = shipperPhone,
                        isReserved = isReserved,
                        reservationTime = reservationTime,
                        notificationOption = notificationOption
                    )

                    repository.updateOrder(updatedOrder)
                    cacheManager.addOrUpdateOrder(updatedOrder)

                    if (selectedOrder?.id == orderId) {
                        selectedOrder = updatedOrder
                    }
                    errorMessage = null
                    memoryToastMessage = "✏️ 오더 정보가 수정되었습니다."
                } else {
                    errorMessage = "수정할 오더를 찾을 수 없습니다."
                }
            } catch (e: Exception) {
                errorMessage = "오더 수정 실패: ${e.localizedMessage}"
            }
        }
    }

    // ==========================================
    // ⏰ [신규 추가] 휴대폰 알람 스누즈 및 예약 제어 기능들
    // ==========================================

    // 1. 예약 취소 (예약 상태 해제 및 대기 상태로 전환 또는 취소 처리)
    fun cancelReservation(orderId: String) {
        viewModelScope.launch {
            try {
                val existing = cacheManager.cachedOrders.find { it.id == orderId } ?: return@launch
                val updated = existing.copy(
                    isReserved = false,
                    reservationTime = "",
                    notificationOption = "예약 취소됨"
                )
                repository.updateOrder(updated)
                cacheManager.addOrUpdateOrder(updated)
                if (selectedOrder?.id == orderId) {
                    selectedOrder = updated
                }
                memoryToastMessage = "🚫 예약이 취소되었습니다."
            } catch (e: Exception) {
                errorMessage = "예약 취소 실패: ${e.localizedMessage}"
            }
        }
    }

    // 2. 예약 무시하고 지금 바로 진행 (즉시 배차 상태로 전환)
    fun proceedOrderNow(orderId: String) {
        viewModelScope.launch {
            try {
                val existing = cacheManager.cachedOrders.find { it.id == orderId } ?: return@launch
                val updated = existing.copy(
                    isReserved = false,
                    reservationTime = "",
                    notificationOption = "즉시 전환됨"
                )
                repository.updateOrder(updated)
                cacheManager.addOrUpdateOrder(updated)
                if (selectedOrder?.id == orderId) {
                    selectedOrder = updated
                }
                memoryToastMessage = "🚀 즉시 진행 오더로 전환되었습니다!"
            } catch (e: Exception) {
                errorMessage = "즉시 진행 전환 실패: ${e.localizedMessage}"
            }
        }
    }

    // 3. 시간 앞당김 또는 뒤로 연기 (스누즈 / 타임 컨트롤 개념)
    fun postponeOrAdvanceReservation(orderId: String, actionType: String) {
        viewModelScope.launch {
            try {
                val existing = cacheManager.cachedOrders.find { it.id == orderId } ?: return@launch

                // 간단한 문자열 조합 제어 또는 시간 가감 처리
                val currentTime = existing.reservationTime.ifBlank { "2026-09-03 14:00" }
                val modifiedTimeTag = when {
                    actionType.contains("앞당김") -> "$currentTime (10분 앞당겨짐)"
                    actionType.contains("연기") -> "$currentTime (30분 연기됨)"
                    else -> currentTime
                }

                val updated = existing.copy(
                    reservationTime = modifiedTimeTag,
                    notificationOption = actionType
                )
                repository.updateOrder(updated)
                cacheManager.addOrUpdateOrder(updated)
                if (selectedOrder?.id == orderId) {
                    selectedOrder = updated
                }
                memoryToastMessage = "⏰ 예약 시간이 조정되었습니다: $actionType"
            } catch (e: Exception) {
                errorMessage = "예약 시간 조정 실패: ${e.localizedMessage}"
            }
        }
    }

    // ==========================================

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

                wikiRouterRepository.startRouterConnection(roomKey)

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