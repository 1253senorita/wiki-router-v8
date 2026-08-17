package com.teminator.mypadnoteone.data.cache

import com.teminator.mypadnoteone.domain.model.DispatchOrder

class OrderCacheManager(
    private val maxCapacity: Int = 100 // 💡 메모리에 유지할 최대 오더 개수 (기본 100개)
) {
    private val _cachedOrders = mutableListOf<DispatchOrder>()
    val cachedOrders: List<DispatchOrder> get() = _cachedOrders

    /**
     * 새로운 오더 리스트를 캐시에 적재합니다.
     * 최대 용량을 초과하면 가장 오래된(먼저 들어온) 항목부터 자동으로 제거합니다.
     */
    fun updateOrders(newOrders: List<DispatchOrder>) {
        _cachedOrders.clear()
        _cachedOrders.addAll(newOrders)
        trimCache()
    }

    /**
     * 단건 오더를 추가하거나 수정합니다 (존재하면 갱신, 없으면 추가).
     */
    fun addOrUpdateOrder(order: DispatchOrder) {
        val index = _cachedOrders.indexOfFirst { it.id == order.id }
        if (index != -1) {
            _cachedOrders[index] = order
        } else {
            _cachedOrders.add(order)
        }
        trimCache()
    }

    /**
     * 특정 ID의 오더를 캐시에서 제거합니다.
     */
    fun removeOrder(orderId: String) {
        _cachedOrders.removeAll { it.id == orderId }
    }

    /**
     * 캐시된 데이터를 모두 비웁니다 (메모리 해제).
     */
    fun clear() {
        _cachedOrders.clear()
    }

    /**
     * 허용된 최대 용량을 초과할 경우 오래된 항목을 잘라내는 내부 메서드
     */
    private fun trimCache() {
        if (_cachedOrders.size > maxCapacity) {
            // 초과된 만큼 앞에서부터 제거 (오래된 순)
            val excessCount = _cachedOrders.size - maxCapacity
            repeat(excessCount) {
                if (_cachedOrders.isNotEmpty()) {
                    _cachedOrders.removeAt(0)
                }
            }
        }
    }
}