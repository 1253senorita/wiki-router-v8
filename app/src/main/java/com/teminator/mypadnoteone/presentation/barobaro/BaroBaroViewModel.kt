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

    init {
        loadOrders() // 뷰모델 생성 시 데이터 로드
    }

    private fun loadOrders() {
        viewModelScope.launch {
            val orders = repository.getOrders()
            _orderList.clear()
            _orderList.addAll(orders)
        }
    }

    fun selectOrder(order: DispatchOrder?) {
        selectedOrder = order
    }

    fun addOrder(route: String, cargo: String, price: String, description: String) {
        viewModelScope.launch {
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
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "수락됨") // 리포지토리 상태 업데이트
            val index = _orderList.indexOfFirst { it.id == orderId }
            if (index != -1) {
                val updated = _orderList[index].copy(status = "수락됨")
                _orderList[index] = updated
                selectedOrder = updated
            }
        }
    }
}