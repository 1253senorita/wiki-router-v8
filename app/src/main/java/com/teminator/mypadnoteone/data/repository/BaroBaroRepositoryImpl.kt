package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import javax.inject.Inject

class BaroBaroRepositoryImpl @Inject constructor() : BaroBaroRepository {

    // 💡 메모리 리스트를 기반으로 데이터를 안전하고 빠르게 관리
    private val memoryOrders = mutableListOf<DispatchOrder>()

    override suspend fun getOrders(): List<DispatchOrder> = memoryOrders

    override suspend fun addOrder(order: DispatchOrder) {
        memoryOrders.add(order)
    }

    // 💡 [수정됨] status와 함께 driverId도 전달받아 메모리 데이터를 안전하게 갱신
    override suspend fun updateOrderStatus(orderId: String, status: String, driverId: String) {
        val index = memoryOrders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            memoryOrders[index] = memoryOrders[index].copy(
                status = status,
                driverId = driverId // 📌 수락한 기사 ID 반영
            )
        }
    }

    override suspend fun updateOrder(order: DispatchOrder) {
        val index = memoryOrders.indexOfFirst { it.id == order.id }
        if (index != -1) {
            memoryOrders[index] = order
        }
    }

    override suspend fun deleteOrder(orderId: String) {
        memoryOrders.removeAll { it.id == orderId }
    }
}