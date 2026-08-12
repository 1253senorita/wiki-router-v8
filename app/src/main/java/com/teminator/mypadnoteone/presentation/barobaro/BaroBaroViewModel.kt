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

    val filteredOrderList: List<DispatchOrder>
        get() = if (searchQuery.isBlank()) {
            _orderList
        } else {
            _orderList.filter {
                it.route.contains(searchQuery, ignoreCase = true) ||
                        it.cargoInfo.contains(searchQuery, ignoreCase = true)
            }
        }

    var selectedOrder by mutableStateOf<DispatchOrder?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // 💡 [신규] 메모리 공간 마련 및 대규모 데이터 처리 상태 토스트용 메시지
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
                // 💡 대규모 데이터 로드 전 메모리 확보 안내 토스트 트리거
                memoryToastMessage = "⚠️ 대규모 화물 데이터 메모리 공간 마련 중..."

                val orders = repository.getOrders()
                _orderList.clear()

                if (orders.isEmpty()) {
                    // 🚀 [500개 오더 폭발적 자동 생성 루프]
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
                                route = "$start ➔ $end [특급배차 #$i]",
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
                _orderList.add(newOrder)
            } catch (e: Exception) {
                errorMessage = "오더 등록 실패: ${e.localizedMessage}"
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