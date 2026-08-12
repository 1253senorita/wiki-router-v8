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
    private val repository: BaroBaroRepository // Hilt를 통해 리포지토리 주입
) : ViewModel() {

    private val _orderList = mutableStateListOf<DispatchOrder>()
    val orderList: List<DispatchOrder> get() = _orderList

    var selectedOrder by mutableStateOf<DispatchOrder?>(null)
        private set

    // 에러 상태 변수 추가
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadOrders() // 뷰모델 생성 시 데이터 로드
    }

    private fun loadOrders() {
        viewModelScope.launch {
            try {
                val orders = repository.getOrders()
                _orderList.clear()
                _orderList.addAll(orders)
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
                repository.addOrder(newOrder) // 리포지토리에 저장
                _orderList.add(newOrder)      // 실시간 UI 반영
            } catch (e: Exception) {
                errorMessage = "오더 등록 실패: ${e.localizedMessage}"
            }
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, "수락됨") // 리포지토리 상태 업데이트
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