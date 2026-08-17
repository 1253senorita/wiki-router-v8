package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaroBaroViewModel @Inject constructor(
    private val repository: BaroBaroRepository
) : ViewModel() {

    private val _orderList = mutableStateListOf<DispatchOrder>()
    val orderList: List<DispatchOrder> get() = _orderList

    var searchQuery by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    // 💡 최신 등록된 항목이 위로 오도록 역순(Reversed)으로 리스트 정렬 및 필터링 적용
    val filteredOrderList: List<DispatchOrder>
        get() {
            val list = if (searchQuery.isBlank()) {
                _orderList
            } else {
                _orderList.filter {
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

    private fun loadOrders() {
        viewModelScope.launch {
            try {
                memoryToastMessage = "⚠️ 대규모 화물 데이터 메모리 공간 마련 중..."

                val orders = repository.getOrders()
                _orderList.clear()

                if (orders.isEmpty()) {
                    val regions = listOf("서울", "인천", "경기", "부산", "대구", "대전", "광주", "울산", "강원", "충남", "전북", "경남", "제주", "경북", "전남")
                    val cargoTypes = listOf("1톤 다스퀵 / 박스", "2.5톤 윙바디", "5톤 카고 / 파레트", "11톤 윙바디 / 톤백", "5톤 냉동탑차", "25톤 대형 트레일러")
                    val prices = listOf("130,000원", "180,000원", "250,000원", "350,000원", "480,000원", "520,000원", "750,000원")

                    val generatedDummyList = mutableListOf<DispatchOrder>()
                    for (i in 1..500) {
                        val start = regions[(i * 3) % regions.size]
                        val end = regions[(i * 7) % regions.size]
                        val cargo = cargoTypes[i % cargoTypes.size]
                        val price = prices[i % prices.size]

                        generatedDummyList.add(
                            DispatchOrder(
                                id = i.toString(),
                                route = "$start ➔ $end",
                                cargoInfo = cargo,
                                price = price,
                                status = "대기중",
                                description = "500개 대기열 시스템 부하 분산 테스트용 오더 데이터입니다 (#$i)"
                            )
                        )
                    }
                    _orderList.addAll(generatedDummyList)
                    memoryToastMessage = "✅ 500개 오더 메모리 적재 완료!"
                } else {
                    _orderList.addAll(orders)
                    memoryToastMessage = "✅ 원격 서버 데이터 동기화 완료!"
                }
            } catch (e: OutOfMemoryError) {
                memoryToastMessage = "❌ 메모리 부족! 임시 캐시를 정리하고 있습니다."
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
                val newOrder = DispatchOrder(
                    id = (_orderList.size + 1).toString(),
                    route = route,
                    cargoInfo = cargo,
                    price = price,
                    status = "대기중",
                    description = description
                )
                repository.addOrder(newOrder)
                // 리스트 끝에 추가하면 reversed()에 의해 화면 최상단에 노출됩니다.
                _orderList.add(newOrder)
            } catch (e: Exception) {
                errorMessage = "오더 등록 실패: ${e.localizedMessage}"
            }
        }
    }

    // 💡 [추가 완료] 기존 오더의 내용을 수정하는 ViewModel 메서드
    fun updateOrder(orderId: String, route: String, cargo: String, price: String, description: String) {
        if (route.isBlank() || cargo.isBlank() || price.isBlank()) {
            errorMessage = "필수 항목을 모두 입력해주세요!"
            return
        }

        viewModelScope.launch {
            try {
                val targetIndex = _orderList.indexOfFirst { it.id == orderId }
                if (targetIndex != -1) {
                    val existingOrder = _orderList[targetIndex]
                    val updatedOrder = existingOrder.copy(
                        route = route,
                        cargoInfo = cargo,
                        price = price,
                        description = description
                    )

                    // 1. Repository(저장소)에 수정 사항 반영
                    repository.updateOrder(updatedOrder)

                    // 2. ViewModel 내부 상태 리스트 실시간 갱신
                    _orderList[targetIndex] = updatedOrder

                    // 3. 현재 상세 보기로 열려있는 오더라면 선택된 오더 정보도 갱신
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

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, "수락됨")
                val index = _orderList.indexOfFirst { it.id == orderId }
                if (index != -1) {
                    val updated = _orderList[index].copy(status = "수락됨")
                    _orderList[index] = updated
                    selectedOrder = updated
                }
            } catch (e: Exception) {
                errorMessage = "상태 변경 실패: ${e.localizedMessage}"
            }
        }
    }
}