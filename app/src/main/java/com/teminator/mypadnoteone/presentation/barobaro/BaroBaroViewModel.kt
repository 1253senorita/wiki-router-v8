package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class DispatchOrder(
    val id: String,
    val route: String,
    val cargoInfo: String,
    val price: String,
    val status: String,
    val description: String = ""
)

@HiltViewModel
class BaroBaroViewModel @Inject constructor() : ViewModel() {
    private val _orderList = mutableStateListOf(
        DispatchOrder("1", "스크린차일의  인자 주입 ➔ 대구 북구", "1톤 카고 / 팔레트", "180,000원", "대기중", "상차 시간 엄수"),
        DispatchOrder("2", "VM 바로벌호 ➔ 부산 해운대", "5톤 윙바디 / 파렛트", "350,000원", "대기중"),
        DispatchOrder("3", "경기 안산 ➔ 광주광역시", "다마스 퀵 / 박스", "120,000원", "대기중")
    )
    val orderList: List<DispatchOrder> get() = _orderList

    var selectedOrder by mutableStateOf<DispatchOrder?>(null)
        private set

    fun selectOrder(order: DispatchOrder?) {
        selectedOrder = order
    }

    fun addOrder(route: String, cargo: String, price: String, description: String) {
        val newOrder = DispatchOrder(
            id = (_orderList.size + 1).toString(),
            route = route,
            cargoInfo = cargo,
            price = price,
            status = "대기중",
            description = description
        )
        _orderList.add(newOrder)
    }

    fun acceptOrder(orderId: String) {
        val index = _orderList.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val updated = _orderList[index].copy(status = "수락됨")
            _orderList[index] = updated
            selectedOrder = updated
        }
    }
}