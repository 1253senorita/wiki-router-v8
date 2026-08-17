package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import javax.inject.Inject

class BaroBaroRepositoryImpl @Inject constructor() : BaroBaroRepository {

    // 💡 디폴트 하드코딩 데이터를 모두 제거하고 빈 리스트로 초기화합니다.
    private val memoryOrders = mutableListOf<DispatchOrder>()

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

    // 💡 전달받은 order의 id를 기준으로 메모리 리스트의 데이터를 새로운 내용으로 교체(수정)
    override suspend fun updateOrder(order: DispatchOrder) {
        val index = memoryOrders.indexOfFirst { it.id == order.id }
        if (index != -1) {
            memoryOrders[index] = order
        }
    }

    // 💡 전달받은 orderId에 해당하는 항목을 메모리 리스트에서 삭제
    override suspend fun deleteOrder(orderId: String) {
        memoryOrders.removeAll { it.id == orderId }
    }
}