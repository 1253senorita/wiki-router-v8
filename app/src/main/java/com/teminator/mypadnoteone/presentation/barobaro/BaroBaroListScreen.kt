package com.teminator.mypadnoteone.presentation.barobaro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun BaroBaroListScreen(
    orderList: List<DispatchOrder>,
    onItemClick: (DispatchOrder) -> Unit,
    viewModel: BaroBaroViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 9.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        // 💡 [상단 안내 및 타이틀 헤더] 목록 화면 타이틀과 설명 영역
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "실시간 배차 오더 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "프래그먼트와 컨테이너 위에 인포트로 가져온 컴포져블 상태 함수에 추가하는 갓 컴포저블! 상태 변경에 따라 실시간 UI가 갱신됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            HorizontalDivider(modifier = Modifier.padding(bottom = 6.dp))
        }

        // 💡 [수정 완료] 뷰모델의 filteredOrderList를 바라보도록 변경하여 검색 필터 즉시 반영
        items(viewModel.filteredOrderList) { order ->
            OrderCardItem(
                order = order,
                onItemClick = { onItemClick(order) },
                onAcceptClick = {
                    val testDriverId = "driver_kim_${System.currentTimeMillis()}"
                    viewModel.acceptOrder(order.id, testDriverId)
                }
            )
        }
    }
}

@Composable
fun OrderCardItem(
    order: DispatchOrder,
    onItemClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    val routeParts = order.route.split("➔").map { it.trim() }
    val departure = routeParts.getOrNull(0) ?: order.route
    val destination = routeParts.getOrNull(1) ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: #${order.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "상태: ${order.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "출발: $departure",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "➔",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )

                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "도착: ${if (destination.isNotBlank()) destination else "미정"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "--기타--: ${order.cargoInfo}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = order.price,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (order.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "비고: ${order.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray,
                    maxLines = 1
                )
            }

            if (order.status == "대기중") {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAcceptClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("오더 수락", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}