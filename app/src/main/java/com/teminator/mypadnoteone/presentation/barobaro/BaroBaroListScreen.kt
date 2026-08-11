package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Fragment와 호환되도록 orderList와 onItemClick을 받도록 수정
@Composable
fun BaroBaroManageScreen(
    orderList: List<DispatchOrder>,
    onItemClick: (DispatchOrder) -> Unit,
    viewModel: BaroBaroViewModel
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- [오직 배차 목록만 보여주는 창] ---
        Text(text = "스크린 리스트 쿨 갱신 화면 ", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(orderList) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = { onItemClick(order) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "구간: ${order.route}", style = MaterialTheme.typography.titleSmall)
                        Text(text = "화물: ${order.cargoInfo}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "운임: ${order.price}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "상태: ${order.status}", color = MaterialTheme.colorScheme.primary)

                        if (order.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "비고: ${order.description}", style = MaterialTheme.typography.bodySmall)
                        }

                        if (order.status == "대기중") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.acceptOrder(order.id) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("수락")
                            }
                        }
                    }
                }
            }
        }
    }
}
// 리스트 아이템 카드 컴포저블
@Composable
fun OrderCardItem(
    order: DispatchOrder,
    onItemClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onItemClick // 카드 클릭 시 상세 화면으로 이동
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "구간: ${order.route}", style = MaterialTheme.typography.titleSmall)
            Text(text = "화물: ${order.cargoInfo}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "운임: ${order.price}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "상태: ${order.status}", color = MaterialTheme.colorScheme.primary)

            if (order.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "비고: ${order.description}", style = MaterialTheme.typography.bodySmall)
            }

            if (order.status == "대기중") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAcceptClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("수락")
                }
            }
        }
    }
}