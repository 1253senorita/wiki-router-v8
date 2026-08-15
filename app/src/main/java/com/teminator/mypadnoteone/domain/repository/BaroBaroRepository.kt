package com.teminator.mypadnoteone.domain.repository

import com.teminator.mypadnoteone.domain.model.DispatchOrder

interface BaroBaroRepository {
    suspend fun getOrders(): List<DispatchOrder>
    suspend fun addOrder(order: DispatchOrder)
    suspend fun updateOrderStatus(orderId: String, status: String)
}