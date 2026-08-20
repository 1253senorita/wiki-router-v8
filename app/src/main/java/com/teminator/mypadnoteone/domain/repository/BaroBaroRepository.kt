package com.teminator.mypadnoteone.domain.repository

import com.teminator.mypadnoteone.domain.model.DispatchOrder

interface BaroBaroRepository {
    suspend fun getOrders(): List<DispatchOrder>
    suspend fun addOrder(order: DispatchOrder)

    // 💡 수락 시 상태뿐만 아니라 수락한 기사 ID(driverId)도 함께 업데이트하도록 변경
    suspend fun updateOrderStatus(orderId: String, status: String, driverId: String)

    // 💡 기존 오더의 상세 정보(경로, 화물정보, 가격, 설명 등)를 통째로 수정하는 함수 계약
    suspend fun updateOrder(order: DispatchOrder)

    // 💡 특정 오더를 삭제하는 함수 계약
    suspend fun deleteOrder(orderId: String)
}