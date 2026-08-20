package com.teminator.mypadnoteone.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaroBaroHybridRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BaroBaroRepository {

    private val ordersCollection = firestore.collection("orders")

    private val memoryOrders = mutableListOf<DispatchOrder>().apply {
        for (i in 1..1) {
            add(
                DispatchOrder(
                    id = "local_$i",
                    route = "지역A ➔ 지역B (로컬)",
                    cargoInfo = "화물 정보 #$i",
                    price = "100,000원",
                    status = "대기중",
                    description = "[로컬 메모리] 앱 시작 직후 즉시 로드된 임시 데이터"
                )
            )
        }
    }

    override suspend fun getOrders(): List<DispatchOrder> {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = ordersCollection.get().await()
                val remoteOrders = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(DispatchOrder::class.java)?.copy(id = doc.id)
                }

                if (remoteOrders.isNotEmpty()) {
                    memoryOrders.clear()
                    memoryOrders.addAll(remoteOrders)
                    Log.d("BaroBaroSync", "🔥 [Firebase 성공] 서버 데이터로 싱크 완료! (총 ${remoteOrders.size}개)")
                } else {
                    Log.d("BaroBaroSync", "📦 [Firebase] 서버에 문서가 없습니다. 로컬 데이터를 유지합니다.")
                }
            } catch (e: Exception) {
                Log.e("BaroBaroSync", "❌ [Firebase 에러] 싱크 실패: ${e.localizedMessage}")
            }
        }

        return memoryOrders
    }

    override suspend fun addOrder(order: DispatchOrder) {
        memoryOrders.add(order)
        try {
            ordersCollection.document(order.id).set(order).await()
            Log.d("BaroBaroSync", "✅ [Firebase 성공] 오더 서버 전송 완료: ${order.id}")
        } catch (e: Exception) {
            Log.e("BaroBaroSync", "❌ [Firebase 에러] 오더 전송 실패: ${e.localizedMessage}")
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: String, driverId: String) {
        val index = memoryOrders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            memoryOrders[index] = memoryOrders[index].copy(
                status = status,
                driverId = driverId
            )
        }
        try {
            ordersCollection.document(orderId)
                .update(
                    mapOf(
                        "status" to status,
                        "driverId" to driverId
                    )
                )
                .await()
            Log.d("BaroBaroSync", "✅ [Firebase 성공] 오더 상태 및 기사 매칭 업데이트 완료: $orderId -> 기사($driverId)")
        } catch (e: Exception) {
            Log.e("BaroBaroSync", "❌ [Firebase 에러] 상태 및 기사 매칭 업데이트 실패: ${e.localizedMessage}")
        }
    }

    override suspend fun updateOrder(order: DispatchOrder) {
        val index = memoryOrders.indexOfFirst { it.id == order.id }
        if (index != -1) {
            memoryOrders[index] = order
        }
        try {
            ordersCollection.document(order.id).set(order).await()
        } catch (_: Exception) {}
    }

    override suspend fun deleteOrder(orderId: String) {
        memoryOrders.removeAll { it.id == orderId }
        try {
            ordersCollection.document(orderId).delete().await()
        } catch (_: Exception) {}
    }
}