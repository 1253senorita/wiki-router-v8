package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class DispatchOrder(
    val id: String,
    val route: String,
    val cargoInfo: String,
    val price: String,
    val status: String
)

class BaroBaroViewModel : ViewModel() {

    private val _orderList = mutableStateListOf(
        DispatchOrder("1", "인천 서구 ➔ 대구 북구", "1톤 카고 / 팔레트", "180,000원", "대기중"),
        DispatchOrder("2", "서울 강서구 ➔ 부산 해운대", "5톤 윙바디 / 파렛트", "350,000원", "대기중"),
        DispatchOrder("3", "경기 안산 ➔ 광주광역시", "다마스 퀵 / 박스", "120,000원", "대기중")
    )
    val orderList: List<DispatchOrder> get() = _orderList

    var selectedOrder by mutableStateOf<DispatchOrder?>(null)
        private set

    fun selectOrder(order: DispatchOrder?) {
        selectedOrder = order
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
    private val _orderList = mutableStateListOf(
        DispatchOrder("1", "인천 서구 ➔ 대구 북구", "1톤 카고 / 팔레트", "180,000원", "대기중"),
        DispatchOrder("2", "서울 강서구 ➔ 부산 해운대", "5톤 윙바디 / 파렛트", "350,000원", "대기중"),
        DispatchOrder("3", "경기 안산 ➔ 광주광역시", "다마스 퀵 / 박스", "120,000원", "대기중")
    )
    val orderList: List<DispatchOrder> get() = _orderList

    var selectedOrder by mutableStateOf<DispatchOrder?>(null)
        private set

    fun selectOrder(order: DispatchOrder?) {
        selectedOrder = order
    }

    fun acceptOrder(orderId: String) {
        val index = _orderList.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val updated = _orderList[index].copy(status = "수락됨")
            _orderList[index] = updated
            selectedOrder = updated
        }
    }

