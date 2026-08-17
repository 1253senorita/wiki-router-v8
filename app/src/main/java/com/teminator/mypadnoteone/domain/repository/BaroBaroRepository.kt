package com.teminator.mypadnoteone.domain.repository

import com.teminator.mypadnoteone.domain.model.DispatchOrder

interface BaroBaroRepository {
    suspend fun getOrders(): List<DispatchOrder>
    suspend fun addOrder(order: DispatchOrder)
    suspend fun updateOrderStatus(orderId: String, status: String)

    // 💡 [추가] 기존 오더의 상세 정보(경로, 화물정보, 가격, 설명 등)를 통째로 수정하는 함수 계약
    suspend fun updateOrder(order: DispatchOrder)
}