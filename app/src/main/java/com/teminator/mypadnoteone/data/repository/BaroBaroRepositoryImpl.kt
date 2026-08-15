package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import javax.inject.Inject

class BaroBaroRepositoryImpl @Inject constructor() : BaroBaroRepository {

    private val memoryOrders = mutableListOf(
        DispatchOrder("1", " 디폴트값 스크린차일의 인자 주입 ➔ 대구 북구", "디폴트값1톤 카고 / 디폴트값 팔레트", "디폴트값180,000원", "디폴트값 대기중", "디폴트값 상차 시간 엄수"),
        DispatchOrder("2", " 디폴트값 VM 바로벌호 ➔ 부산 해해운대", "디폴트값5톤 윙바디 / 디폴트값 파렛트", "디폴트값 350,000원", "디폴트값 대기중"),
        DispatchOrder("3", " 디폴트값 경기 안산 ➔ 광주광역시", "디폴트값다마스 퀵 / 디폴트값 박스", "디폴트값120,000원", "디폴트값 대기중")
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