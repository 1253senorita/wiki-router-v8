package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import javax.inject.Inject

class BaroBaroRepositoryImpl @Inject constructor() : BaroBaroRepository {

    private val memoryOrders = mutableListOf(
        DispatchOrder("1", "스크린차일의 인자 주입 ➔ 대구 북구", "1톤 카고 / 팔레트", "180,000원", "대기중", "상차 시간 엄수"),
        DispatchOrder("2", "VM 바로벌호 ➔ 부산 해운대", "5톤 윙바디 / 파렛트", "350,000원", "대기중"),
        DispatchOrder("3", "경기 안산 ➔ 광주광역시", "다마스 퀵 / 박스", "120,000원", "대기중")
    )

    override suspend fun getOrders(): List<DispatchOrder> = memoryOrders

    override suspend fun addOrder(order: DispatchOrder) {
        memoryOrders.add(order)
    }

    override suspend fun updateOrderStatus(orderId: String, status: String) {
        val index = memoryOrders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            memoryOrders[index] = memoryOrders[index].copy(status = status)
        }
    }
}